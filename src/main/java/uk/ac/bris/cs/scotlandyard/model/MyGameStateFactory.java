package uk.ac.bris.cs.scotlandyard.model;

import ch.qos.logback.core.pattern.IdentityCompositeConverter;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

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
			return 0;
		}
	}




	//step 2:GAME STATE
	// 内部类 MyGameState 实现了 GameState 接口
	//我去除了final，因此这些量变成了不可变量，注意如果出错可改
	//请注意，构造函数是私有的，因为只有封闭类中的构建器（以及后来的 advance 方法）才会调用它。
	// 下面的@Nonnull
	//		@Override的创建都属于step2
	private final class MyGameState implements GameState{
		private  final GameSetup setup;
		private final Player mrX;
		private final ImmutableList<Player> detectives;
		private final ImmutableList<LogEntry> log;
		private final ImmutableSet<Piece> remaining;

		// 存储了每个玩家的 TicketBoard 对象，方便快速查找
		private final ImmutableSet<TicketBoard> ticketBoards;

		//Step 3:CONSTRUCTOR 构造函数
		public MyGameState(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
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
			for (Player detective : detectives){
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
			return null;
		}

		@Nonnull
		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return Optional.empty();
		}

		@Nonnull
		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Piece> getWinner() {
			return null;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return null;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			return null;
		}

	}
}










