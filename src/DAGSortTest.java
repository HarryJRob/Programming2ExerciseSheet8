import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

//Tests start at line 175
@RunWith(Enclosed.class)
public class DAGSortTest {
	//Methods used for test
	
	//Generate a random directed, acyclic adjacency list
	private static int[][] generateRandomAdjList(int numberOfNodes) {
		
		//Generate a acyclic directed adjacency matrix
		int[][] adjMatrix = new int[numberOfNodes][numberOfNodes];
		
		for(int y = 0; y < numberOfNodes; y++) {
			for(int x = 0; x < numberOfNodes; x++) {
				adjMatrix[x][y] = 0;
			}
		}
		
		int toCheck = 0;
		for(int y = 0; y < numberOfNodes; y++) {
			
			for(int x = 0; x < toCheck; x++) {
				adjMatrix[y][x] = (int) Math.round(Math.random());
			}
			toCheck++;
		}
		
		int[][] adjList = new int[numberOfNodes][];
		
		//displayAdjMatrix(adjMatrix);
		
		//Convert to a adjacency list
		for(int y = 0; y < numberOfNodes; y++) {

			ArrayList<Integer> curNodeAdjList = new ArrayList<Integer>();
			
			for(int x = 0; x < numberOfNodes; x++) {
				
				if(adjMatrix[y][x] == 1) {
					curNodeAdjList.add(x);
				}
			}
			
			adjList[y] = integerListToIntArray(curNodeAdjList); 
		}
		
		//return the randomly generated acyclic adjacency list
		return adjList;
	}
	
	//Converts a List<Integer> to int[]
	//Has to be static to be used by TopologicalSorter
	private static int[] integerListToIntArray(List<Integer> integerList) {
		int[] returnArray = new int[integerList.size()];
		
		for(int i = 0; i < integerList.size(); i++) {
			returnArray[i] = (int) integerList.get(i);
		}
		
		return returnArray;
	}

	//Compare the actuals to all possible outputs. If they match then the Topological sort was correct.
	private static boolean checkValidDAGSort(int[][] expectedOutputs, int[] actualOutput) {

		for(int[] curExpected : expectedOutputs) {
			if(Arrays.equals(curExpected, actualOutput)) {
				return true;
			}
		}
		
		return false;
	}
	
	//Produces all possible topological sort outputs for a given DAG
	//This is based off of the article seen below:
	//https://www.geeksforgeeks.org/all-topological-sorts-of-a-directed-acyclic-graph/
	private static class TopologicalSorter {
		
		//Store the number of vertices
		int numberOfVertexs;
		
		//Stores if the vertex has been visited or not
		boolean[] vertexsVisited;
		
		//Stores the number of edges connected to each node
		int[] numConnectedEdges;
		
		//Store the adjacencyList
		int[][] adjList;
		
		//Stores the current order of vertices being explored
		ArrayList<Integer> vertexList;
		
		ArrayList<List<Integer>> outputList;
		
		public TopologicalSorter(int[][] adjList) {
			this.adjList = adjList;
			numberOfVertexs = adjList.length;
			vertexsVisited = new boolean[numberOfVertexs];
			numConnectedEdges = new int[numberOfVertexs];
			vertexList = new ArrayList<Integer>();
			outputList = new ArrayList<List<Integer>>();
			
			//Construct numConnectedEdges
			for(int y = 0; y < numberOfVertexs; y++) {
				for(int x = 0; x < adjList[y].length; x++) {
					numConnectedEdges[adjList[y][x]]++;
				}
			}
		}
		
		private void calcAllTopologicalSortSolutions() {
			
			boolean isFinished = false;
			
			//For every vertex
			for(int i = 0; i < numberOfVertexs; i++) {
				
				//Look at the unvisited vertices with 0 connected edges
				if(!vertexsVisited[i] && numConnectedEdges[i] == 0) {
					//Set this vertex as marked and remove edges connecting it to other vertices
					vertexsVisited[i] = true;
					for(int x = 0; x < adjList[i].length; x++) {
						numConnectedEdges[adjList[i][x]]--;
					}
					
					//Adds this vertex to the list of currently explored vertices
					vertexList.add(i);
					calcAllTopologicalSortSolutions();
					
					//Backtracking
					vertexsVisited[i] = false;
					for(int x = 0; x < adjList[i].length; x++) {
						numConnectedEdges[adjList[i][x]]++;
					}
					vertexList.remove(vertexList.indexOf(i));
					
					isFinished = true;
				}
				
			}
			
			//All vertices have been visited and so this is a possible order
			if(!isFinished) {
				outputList.add((List<Integer>) vertexList.clone());
			}
			
		}
		
		public int[][] getOutput() {
			int[][] output = new int[outputList.size()][];
			for(int i = 0; i < outputList.size(); i++) {
				//System.out.println(outputList.get(i).toString());
				output[i] = integerListToIntArray(outputList.get(i));
			}
			
			return output;
		}
	}
	
	
	//Testing code:
	
	@RunWith(Parameterized.class)
	public static class ParameterisedTests {
		
		int[][] testDAG;
		
	    public ParameterisedTests(int[][] input) {
	    	testDAG = input;
	    }
		
	    //Creates 10 random DAGs to be used in the sort test
	    @Parameters
	    public static Collection<Object[]> data() {
	        return Arrays.asList(new Object[][] {     
	        	{generateRandomAdjList(10)}, {generateRandomAdjList(10)}, {generateRandomAdjList(10)}
	           });
	    }
	    
	    //Tests if a random DAG of 10 vertices is sorted correctly 
		@Test
		public void DAGSortTest() throws CycleDetectedException, InvalidNodeException {
			int[] output = DAGSort.sortDAG(testDAG);
			
			TopologicalSorter topoSort = new TopologicalSorter(testDAG);
			topoSort.calcAllTopologicalSortSolutions();
			int[][] expectedOutputs = topoSort.getOutput();
			
			assertTrue(checkValidDAGSort(expectedOutputs, output));
		}
		
	}
	
	public static class UnparameterisedTests {
		
		@Test(expected = CycleDetectedException.class)
		public void DGCycleTest() throws CycleDetectedException, InvalidNodeException {
			int[][] testGraph = new int[][] {
				{4},
				{0},
				{1},
				{2},
				{3}
			};
			
			DAGSort.sortDAG(testGraph);
		}
		
		@Test(expected = NullPointerException.class) 
		public void DAGNullTest() throws CycleDetectedException, InvalidNodeException {
			DAGSort.sortDAG(null);
		}

		@Test(expected = InvalidNodeException.class)
		public void DAGInvalidNodeTest() throws CycleDetectedException, InvalidNodeException {
			int[][] testGraph = new int[][] {
				{1},
				{},
				{1, 3},
				{0},
				{0, 1, 5}
			};
			
			DAGSort.sortDAG(testGraph);
		}
		
	}
	
}
