package solver;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FifteenPuzzleSolverC 
{

	private ExecutorService threadPool;
	private ConcurrentLinkedQueue<Future<List<BoardC>>> myResultQueue;
	private int threadNum;

	public FifteenPuzzleSolverC(int threadCount)
	{
		if (threadCount > 1) 
		{
			this.threadNum = threadCount;
			myResultQueue = new ConcurrentLinkedQueue<>();
			threadPool = Executors.newFixedThreadPool(threadCount);
		}
	}
	
	public List<BoardC> solveFactory(BoardC b) throws InterruptedException, ExecutionException 
	{
		int baseDepth = b.minimumSolutionDepth();
		int highestThreadDepth = threadNum + baseDepth;
	
		for (int i = baseDepth; i <= highestThreadDepth; i++)
		{
			SolveC solveTask = new SolveC(b, i);
			Future<List<BoardC>> future = threadPool.submit(solveTask);
			myResultQueue.add(future);
		}
		
		while(true) 
		{
			if (myResultQueue.peek().isDone())
			{
				List<BoardC> ret = myResultQueue.poll().get();
				if  (ret != null)
				{
					threadPool.shutdownNow();
					return ret;
				}
			}
			else 
			{
				highestThreadDepth++;
				SolveC call = new SolveC(b, highestThreadDepth);
				Future<List<BoardC>> newFuture = threadPool.submit(call);
				myResultQueue.add(newFuture);
			}//end else
		}
	}
	
	
	public List<BoardC> solve (BoardC board) 
	{
					
			int maxDepth = board.minimumSolutionDepth();
			
			// note: program searches forever.  At each iteration, it searchers for solutions that
			// have an increasing number of maximum moves (as reflected in maxDepth).
			while (true) 
			{
				List<BoardC> solution = doSolve(board,0,maxDepth);
				
				if (solution != null) 
				{
					return solution;
				}
				else 
				{
					maxDepth++; // search again, with a larger maxDepth
				}
			}
	}
	
	
	public static List<BoardC> doSolve(BoardC board, int currentDepth, int maxDepth)
	{
		if (board.isSolved())
		{
			List<BoardC> list = new LinkedList<>();
			list.add(board);
			return list;
		}
	
	
		if ((currentDepth + board.minimumSolutionDepth()) > maxDepth)
		{
			return null;
		}
		List <BoardC> nextMoves = board.generateSuccessors();
		for(BoardC nextBoard : nextMoves)
		{
			List<BoardC> solution = doSolve(nextBoard, currentDepth + 1, maxDepth);
			if  (solution != null) 
			{
				solution.add(0,board);
				return solution;
			}
		}
		return null;
	}
	
	
	public static void main (String[] args) throws InterruptedException, ExecutionException, IOException 
	{
	int threadCount = 2;
	
		if (args.length >=1) 
		{
			try 
			{
				threadCount = Integer.parseInt(args[0]);
				if (threadCount <=1)
				{
					threadCount = 1;
				}
			}
			catch (NumberFormatException e)
			{
				System.out.println("invalid input defaulting to 1 thread.");
				threadCount=1;
			}
		}
	
		FifteenPuzzleSolverC fps = new FifteenPuzzleSolverC(threadCount);
		BoardC board = BoardC.createBoard();
	
		System.out.println("Using " + threadCount + " threads to solve this board: \n" + board);
		System.out.println();
	
		List<BoardC> solution;
		long elapsed;
	
		if (threadCount == 1)
		{
			long ts = System.currentTimeMillis();
			solution = fps.solve(board);
			elapsed = System.currentTimeMillis() - ts;
		}
		else 
		{
			long ts = System.currentTimeMillis();
			solution = fps.solveFactory(board);
			elapsed = System.currentTimeMillis() - ts;
		}
	
		System.out.println("Found a solution with " + solution.size() +  " moves!");
		for (int i = 0; i<solution.size(); i++)
		{
			System.out.println(solution.get(i));
		}
		System.out.println("Elapsed time: " + ((double) elapsed) / 1000.0 + " seconds");
	}
}
