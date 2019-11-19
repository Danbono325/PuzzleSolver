package solver;
import java.util.List;
import java.util.concurrent.Callable;

public class SolveC implements Callable<List<BoardC>>
{
	private final BoardC board;
	private final int depth;
	
	public SolveC(BoardC board, int depth) 
	{
		this.board = board;
		this.depth = depth;
	}
	
	@Override
	public List<BoardC> call() throws Exception 
	{
		return FifteenPuzzleSolverC.doSolve(board, 0, depth);
	}
}

