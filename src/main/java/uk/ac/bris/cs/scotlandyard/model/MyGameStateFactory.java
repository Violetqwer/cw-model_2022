package uk.ac.bris.cs.scotlandyard.model;

import ch.qos.logback.core.pattern.IdentityCompositeConverter;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.*;


import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;



import java.util.*;
import java.util.stream.Stream;
import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.Sets;

import com.google.common.graph.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {


	//step 1:ATTRIBUTES
	@Nonnull
	@Override
	public GameState build(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
		// TODO
		// 创建 MyGameState 实例，并将 setup、mrX 和 detectives 作为参数传递
		MyGameState gameState = new MyGameState(setup, mrX, detectives);

		// 返回新创建的 MyGameState 实例
		return gameState;
	}


	//step3 需要使用的TicketBoardImpl 类作为MyGameStateFactory的内部类
	//TicketBoardImpl 类是用来实现 Board.TicketBoard接口的
	//将票务信息(例如bus，taxi。。。)封装在一个独立的类中，并直接实现了 TicketBoard 接口
	private class TicketBoardImpl implements Board.TicketBoard {
		private final Player player;
		private final GameSetup setup;
		public TicketBoardImpl(Player player, GameSetup setup){
			this.player = player;
			this.setup = setup;
		}

		@Override
		public int getCount(@Nonnull Ticket ticket) {

			return player.tickets().get(ticket);
		}
	}




	//step 2:GAME STATE
	// 内部类 MyGameState 实现了 GameState 接口
	//我去除了final，因此这些量变成了不可变量，注意如果出错可改
	//请注意，构造函数是私有的，因为只有封闭类中的构建器（以及后来的 advance 方法）才会调用它。
	// 下面的@Nonnull
	//		@Override的创建都属于step2
	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private final Player mrX;
		private final ImmutableList<Player> detectives;
		private final ImmutableList<LogEntry> log;
		private final ImmutableSet<Piece> remaining;

		// 存储了每个玩家的 TicketBoard 对象，方便快速查找
		private final ImmutableSet<TicketBoard> ticketBoards;

		//Step 3:CONSTRUCTOR 构造函数
		public MyGameState(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {

			//step 4的初始化检查: 添加参数的非空检查
			if (setup == null || mrX == null || detectives == null) {
				throw new IllegalArgumentException("setup, mrX, and detectives cannot be null.");
			}
			if (setup.moves.isEmpty()) {
				throw new IllegalArgumentException("move is empty!");
			}

			this.setup = setup;
			this.mrX = mrX;
			this.detectives = detectives;

			//step 4：INITIALISATION
			//初始化log和remaining
			this.log = ImmutableList.of();

			//初始化remaining
			this.remaining = Stream.concat(Stream.of(mrX.piece()),
					detectives.stream().map(Player::piece)).collect(ImmutableSet.toImmutableSet());

			//初始化ticketBoards
			ArrayList<TicketBoard> boards = new ArrayList<>();
			boards.add(new TicketBoardImpl(mrX, setup));
			for (Player detective : detectives) {
				boards.add(new TicketBoardImpl(detective, setup));
			}
			this.ticketBoards = ImmutableSet.copyOf(boards);


		}


		@Nonnull
		@Override
		public GameSetup getSetup() {
			return setup;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getPlayers() {
			//创建一个包含所有玩家的集合
			ImmutableSet.Builder<Piece> players = ImmutableSet.builder();

			//add Mr.X
			players.add(mrX.piece());

			//add detective
			for(Player detective : detectives){
				players.add(detective.piece());
			}

			//返回构建的不可变集合
			return players.build();
		}


		//step 5
		//根据注释的意思，需要遍历所有的警探（detective），
		// 如果某个警探的棋子（Piece）和输入的参数（Piece.Detective detective）相同，
		// 则返回该警探的位置，否则返回 Optional.empty()。
		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			//用for循环所有detectives
			for (Player player : detectives) {
				//如果当前detectives的pieces 与指定的棋子类型相同
				if (player.piece() == detective) {
					//return 该警探位置
					return Optional.of(player.location());
				}
			}
			//如果没有匹配，返回Optional.empty（）
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {

			return ticketBoards.stream().filter(ticketBoard -> ticketBoard.piece() == piece).findFirst();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {

			return log;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			//check游戏状态，确定哪一方胜利
			if (/*MrX获胜*/){
				return ImmutableSet.of(Piece.MrX);

			}else if (/*detective获胜*/){
				//创建一个包含所有侦探的集合
				ImmutableSet.Builder<Piece> winners = ImmutableSet.builder();

				//add all detectives
				for (Player detective : detectives){
					winners.add(detective.piece());
				}

				return winners.build();
			}else{
				//游戏尚未结束，返回empty不可变集合
				return ImmutableSet.of();
			}

			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//创建一个ImmutableSet.Builder对象，用于构建最终返回的不可变集合
			ImmutableSet.Builder<Move> moves = ImmutableSet.builder();

			//将mr.X 的singlemove加入moveslist
			moves.addAll(makeSingleMoves(mrX));

			//循环所有侦探，将每个侦探的单步移动加入moves集合
			for (Player player : detectives) {
				moves.addAll(makeSingleMoves(player));
			}

			//create 不可变集合并返回
			return moves.build();
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			return null;
		}


		//创建SingleMove的构造方法
		public static class SingleMove implements Move {
			private final Piece piece;
			private final Ticket ticket;
			private final int destination;

			public SingleMove(Piece piece, Ticket ticket, int destination){
				this.piece = piece;
				this.ticket = ticket;
				this.destination = destination;
			}

			@Override
			public Piece piece(){
				return piece;
			}

			@Override
			public Ticket ticket(){
				return ticket;

			}

			@Override
			public int destination(){
				return destination;
			}

			@Override
			public boolean isDoubleMove(){
				return false;
			}

		}








		//step 6 AVAILABLE MOVES

		//1.创建三个private方法
		//isDestinationOccupied：检查给定的节点是否已经被占据
		private boolean isDestinationOccupied(int destinationNode){
			for (Player detective : detectives){
				if (detective.location() == destinationNode){
					return true;
				}
			}
			return false;
		}

		//
		private ScotlandYard.Ticket fromTransport(ScotlandYard.Transport transport){
			switch (transport){
				case TAXI:
					return Ticket.TAXI;
				case BUS:
					return Ticket.BUS;
				case UNDERGROUND:
					return Ticket.UNDERGROUND;
				default:
					throw new IllegalArgumentException("Invalid transport type UWU");
			}
		}

		private boolean hasTickets(Player player, Ticket ticket){
			return player.tickets().get(ticket) > 0;
		}







		// 创建一个HashSet来储存单步移动对象
		private Set<Move> makeSingleMoves(Player player) {
			HashSet<Move> moves = new HashSet<>();

			// 获取万千当前节点
			int currentNode = player.location();

			// 遍历当前节点所有相邻节点
			for (Integer destination : setup.graph.adjacentNodes(currentNode)) {

				//获取当前节点与目标节点之间的边的 Transport 类型
				Set<ScotlandYard.Transport> transports = setup.graph.edgeValueOrDefault(currentNode, destination, ImmutableSet.of());

				// 检查目标节点是否可用
				if (!isDestinationOccupied(destination)) {

					// 循环支持的所有交通
					for (ScotlandYard.Transport transport : transports) {

						// 将Transport转换成对应票的类型
						Ticket ticket = fromTransport(transport);

						// Check if the player has the corresponding ticket
						if (player.tickets().get(ticket) > 0) {

							// If so, create a new single move and add it to the moves set
							Move.SingleMove newMove = new Move.SingleMove(player.piece(),ticket,destination);
							moves.add(newMove);


						}
					}
				}
			}

			// Check if Mr.X has any double move tickets available
			if (player.piece() == Piece.MrX && player.has(Ticket.DOUBLE)) {
				moves.addAll(makeDoubleMoves(player, moves));
			}

			return moves;
		}

		private Set<Move.DoubleMove> makeDoubleMoves(Player player, Set<Move.SingleMove> singleMoves) {
			HashSet<Move.DoubleMove> doubleMoves = new HashSet<>();

			for (Move.SingleMove firstMove : singleMoves) {

				// Get the intermediate node from the first single move
				int intermediateNode = firstMove.destination;

				// Loop through all edges from the intermediate node
				for (Edge<Integer, ScotlandYard.Transport> edge : setup.graph.getEdgesFrom(intermediateNode)) {

					// Get the destination node of the edge
					int destinationNode = edge.destination.value();

					// Check if the destination node is available
					if (!isDestinationOccupied(destinationNode)) {

						// Loop through all transport types supported by the edge
						for (ScotlandYard.Transport transport : edge.data()) {

							// Convert the transport to the corresponding ticket type
							Ticket ticket = Ticket.fromTransport(transport);

							// Check if the player has the corresponding ticket
							if (player.has(ticket) && player.has(Ticket.DOUBLE)) {

								// If so, create a new double move and add it to the doubleMoves set
								Move.DoubleMove newDoubleMove = new Move.DoubleMove(player.piece(),
										firstMove.ticket(), firstMove.destination(),
										ticket, destinationNode);
								doubleMoves.add(newDoubleMove);
							}
						}
					}
				}
			}

			return doubleMoves;
		}
	}
}













