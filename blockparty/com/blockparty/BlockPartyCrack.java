package com.blockparty;

import com.games.Games;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import realcraft.bukkit.utils.BlockUtil;
import realcraft.bukkit.utils.LocationUtil.BlockLocation;
import realcraft.bukkit.utils.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockPartyCrack implements Runnable {

	private static final int BLOCK_MAX_DAMAGE = 9;
	private static final int BLOCK_MAX_STEP = 70;

	private BlockParty game;
	private BlockLocation minLoc;
	private BlockLocation maxLoc;
	private Vector minBound;
	private Vector maxBound;

	private BlockPartyCrackState state = BlockPartyCrackState.NONE;
	private HashMap<BlockLocation, BlockPartyCrackBlock> blocks = new HashMap<>();
	private ArrayList<BlockPartyCrackBlock> blockList;

	private int ticks;

	public BlockPartyCrack(BlockParty game, Location loc1, Location loc2){
		this.game = game;

		this.minLoc = new BlockLocation(loc1.getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
		this.maxLoc = new BlockLocation(loc2.getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
		this.minBound = this.minLoc.getLocation().add(-0.5, 1, -0.5).toVector();
		this.maxBound = this.maxLoc.getLocation().add(1.5, 2.5, 1.5).toVector();

		this._initBlocks();

		Bukkit.getScheduler().runTaskTimer(Games.getInstance(), this, 5, 5);
	}

	@Override
	public void run() {
		this._runBlocks();
	}

	private void _initBlocks() {
		blocks.clear();

		for (int x = minLoc.getX(); x <= maxLoc.getX(); x++) {
			for (int y = minLoc.getY(); y <= maxLoc.getY(); y++) {
				for (int z = minLoc.getZ(); z <= maxLoc.getZ(); z++) {
					blocks.put(new BlockLocation(minLoc.getLocation().set(x, y, z)), new BlockPartyCrackBlock(minLoc.getLocation().set(x, y, z).getBlock()));
				}
			}
		}

		blockList = new ArrayList<>(blocks.values());
	}

	private void _runBlocks() {
		ticks ++;

		if (ticks >= 3) {
			ticks = 0;
			state = this._hasPlayersInside() ? BlockPartyCrackState.ACTIVE : BlockPartyCrackState.NONE;

			if (state == BlockPartyCrackState.ACTIVE) {
				BlockPartyCrackBlock block = this.getRandomClearBlock();
				if (block != null) {
					block.setStep(1);
				}
			}
		}

		for (BlockPartyCrackBlock block : blocks.values()) {
			if (block.getStep() > 0) {
				block.setStep(block.getStep() + (state == BlockPartyCrackState.ACTIVE ? 1 : -1));

				if (block.getStep() == BLOCK_MAX_DAMAGE + 2) {
					block.destroy();
				} else if (block.getStep() >= BLOCK_MAX_STEP) {
					block.create();
					this._sendBlockDamage(block.getBlock().getLocation(), 10);
				} else if (block.getStep() == 1) {
					block.create();
					this._sendBlockDamage(block.getBlock().getLocation(), 10);
				}
			}

			if (block.getStep() > 0 && block.getStep() <= BLOCK_MAX_DAMAGE + 1) {
				this._sendBlockDamage(block.getBlock().getLocation(), block.getDamage());
			}
		}
	}

	private BlockPartyCrackBlock getRandomClearBlock() {
		return this.getRandomClearBlock(0);
	}

	private BlockPartyCrackBlock getRandomClearBlock(int recursionStep) {
		BlockPartyCrackBlock block = blockList.get(RandomUtil.getRandomInteger(0, blockList.size() - 1));

		if (block.getStep() != 0) {
			if (recursionStep > (blocks.size() * 2)) {
				return null;
			}

			return this.getRandomClearBlock(recursionStep + 1);
		}

		return block;
	}

	private boolean _hasPlayersInside() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (this._isPlayerInside(player)) {
				return true;
			}
		}

		return false;
	}

	private boolean _isPlayerInside(Player player) {
		return player.getLocation().toVector().isInAABB(this.minBound, this.maxBound);
	}

	private void _sendBlockDamage(Location location, int damage) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BlockUtil.sendBlockDamage(player, location, damage);
		}
	}

	private static class BlockPartyCrackBlock {

		private final Block block;
		private final Material type;
		private int step;

		public BlockPartyCrackBlock(Block block) {
			this.block = block;
			this.type = block.getType();
		}

		public Block getBlock() {
			return block;
		}

		public void setStep(int step) {
			this.step = step;
		}

		public int getStep() {
			return step;
		}

		public int getDamage() {
			return step > 0 && step < 12 ? step - 2 : 0;
		}

		public void create() {
			this.setStep(0);
			this.getBlock().setType(this.type);
		}

		public void destroy() {
			this.getBlock().breakNaturally(true);
		}
	}

	private enum BlockPartyCrackState {
		ACTIVE, NONE
	}
}
