package finalPrj;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

//import com.mysql.cj.xdevapi.Statement;

public class Recommendation {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/finalproject";
	
	static final String USER = "root";
	static final String PASS = "gPals1120";
	
	public static void main(String[] args) 
	{
		Connection conn = null;
		Statement stmt = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			System.out.println("Connecting to database");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
			System.out.println("Creating statement");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * FROM movies";
			ResultSet rs = stmt.executeQuery(sql);
			
//			HashSet<Integer> keywordList = new HashSet<Integer>();
//			
//			
//			while(rs.next())
//			{
//				String keywordCol = rs.getString("keywords");
//				//keywordCol = keywordCol.substring(1, keywordCol.length()-1);
//				
//				JSONArray jarr = new JSONArray(keywordCol);
//				for(int i =0; i < jarr.length(); i++)
//				{
//					//String str_alldata = jarr.getJSONObject(i).getString(key);
//					//JSONArray newjarr = new JSONArray(str_alldata);
////					for(int j = 0; j < newjarr.length(); j++)
////					{
////						String name = newjarr.getJSONObject(j).getString("Name");
////                        String id   = newjarr.getJSONObject(j).getString("E_ID");
////
////                        System.out.println(">>-- NAME  :   "+name);                        
////                        System.out.println(">>-- E_ID  :   "+id); 
////					}
//					System.out.println(jarr.getJSONObject(i).getInt("id"));
//					keywordList.add(jarr.getJSONObject(i).getInt("id"));
//				}
//				
//				
////				JSONObject jObject = new JSONObject(keywordCol);
////				Iterator<?> keys=jObject.keys();
////				while(keys.hasNext())
////				{
////					String key = (String)keys.next();
////					System.out.println(key);
////				}
//				String title = rs.getString("title");
//				//System.out.println(jObject.keySet());
//				System.out.println(keywordCol);
//			}
//			System.out.println("finalList");
//			System.out.println(keywordList);
//			System.out.println(keywordList.size());
//			String sql2;
//			sql2 = "CREATE TABLE keywordsByMovie(movie varchar(100)  ";
//			
//			for(int s : keywordList)
//			{
//				sql2 += ",D"+s+ " boolean \n";
//				
//						
//			}
//			sql2 += ");";
//			System.out.println(sql2);
//			int rs2 = stmt.executeUpdate(sql2);
			
			System.out.println("Enter name of the movie:");
	        Scanner rd = new Scanner(System.in);
	        String movieName = rd.nextLine();
	        
	        int x = 10;
			
			System.out.println(findXClosestMovies( movieName,  x));
			ArrayList<String> top10 = new ArrayList<>();
			top10 = (ArrayList<String>) findXClosestMovies( movieName,  x);
			SortedMap<Double,String> top10hashed = new TreeMap<>();
			int w = 5;
			top10hashed = famous(top10, w);
			
			//System.out.println(top10hashed.keySet());
			System.out.println("The Top 5 Recommendation movies from your selected movie are:"+top10hashed.values().toString());
			//int w = 5;
		}
		catch(Exception e)
		{
			System.out.println("connection fail");
			e.printStackTrace();
		}
		finally
		{
			try {
				if(stmt!=null)
					stmt.close();
			}
			catch(Exception e2)
			{}
			try {
				if(conn!=null)
					conn.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	public static List<String> findXClosestMovies(String movieName, int x)
	{
		//System.out.println("asdfasdf");
		Connection conn = null;
		Statement stmt = null;
		SortedMap<Integer, ArrayList<String>> recommendedMovies = new TreeMap<Integer, ArrayList<String>>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			System.out.println("Connecting to database");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
			System.out.println("Creating statement");
			stmt = conn.createStatement();
			
			String sql;
			sql = "SELECT credits.cast1, movies.keywords, movies.genres FROM movies inner join credits on movies.title = credits.title where movies.title ="+ "'"+ movieName+"'";   
			//System.out.println(sql);
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> movie1Attribute = new ArrayList<String>();
			rs.next();
			movie1Attribute.add(rs.getString("credits.cast1"));
			movie1Attribute.add(rs.getString("movies.genres"));
			movie1Attribute.add(rs.getString("movies.keywords"));
			
			
			System.out.println(movie1Attribute);
			
			sql = "Select credits.cast1, movies.title, movies.keywords, movies.genres from movies inner join credits on movies.title = credits.title where movies.title != "+ "'"+movieName+"' limit 100";
			//System.out.println(sql);
			ResultSet rsAllMovies = stmt.executeQuery(sql);
			ArrayList<String> movie2Attribute = new ArrayList<String>();
			
			//System.out.println("werasdrasr");
			
			//SortedMap<Integer, ArrayList<String>> recommendedMovies;
			int curNumMovies = 0;
			
			while( rsAllMovies.next())
			{
				//System.out.println("inside the while");
				movie2Attribute = null;
				movie2Attribute = new ArrayList<String>(Arrays.asList(rsAllMovies.getString("cast1"),rsAllMovies.getString("genres"), rsAllMovies.getString("keywords")));
				String curMovie = rsAllMovies.getString("title");
				//System.out.println(curMovie);
				int distance = getDistanceBetweenTwoMovies(movie1Attribute, movie2Attribute);
				//System.out.println(distance);
				if(curNumMovies < x)
				{
					if(recommendedMovies.containsKey(distance)){
			            recommendedMovies.get(distance).add(curMovie);
			        } else{
			            recommendedMovies.put(distance,new ArrayList<String>(Arrays.asList(curMovie)));
			        }
			        curNumMovies++;
			        continue;
				}
				if(distance >= recommendedMovies.lastKey())
				{
					continue;
				}
				if(recommendedMovies.containsKey(distance)){//put in new Movie
		            recommendedMovies.get(distance).add(curMovie);
		        } else{
		            recommendedMovies.put(distance,new ArrayList<String>(Arrays.asList(curMovie)));
		        }
		        curNumMovies++;
		        
		        ArrayList<String> lastKeyArray = recommendedMovies.get(recommendedMovies.lastKey());
		        if(lastKeyArray.size() >1){
		            lastKeyArray.remove(0);
		        } else {
		            recommendedMovies.remove(recommendedMovies.lastKey());
		        }
		        curNumMovies--;
				
			}
			//ArrayList<String> recommendedMovies = new ArrayList<String>();
			//System.out.println(recommendedMovies.keySet());
			
		}
		catch(Exception e)
		{
			System.out.println("connection fail");
			e.printStackTrace();
		}
		finally
		{
			try {
				if(stmt!=null)
					stmt.close();
			}
			catch(Exception e2)
			{}
			try {
				if(conn!=null)
					conn.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		ArrayList<String> returnList = new ArrayList<>();
		if(!recommendedMovies.isEmpty())
		{
			for (ArrayList<String> curArray: recommendedMovies.values()){
			    returnList.addAll(curArray);
			}
		}
		
		
		
		return returnList;
		//int distance = getDistanceBetweenTwoMovies(movie1Addtributes, movie2Attributes);
	}
	
	public static int getDistanceBetweenTwoMovies(List<String> movie1Attributes, List<String> movie2Attributes)
	{
		int distance = 0;
		if(movie1Attributes.size() != movie2Attributes.size())
		{
			System.out.println("Error: attribute arrays differnet sizes!");
			return -1;
		}
		for(int i=0; i < movie1Attributes.size(); i++)
		{
			ArrayList<String> currentMovie1Attribute = new ArrayList<>();
		    ArrayList<String> currentMovie2Attribute = new ArrayList<>();
		    
		    
		    JSONArray jarr = new JSONArray(movie1Attributes.get(i));
			for(int k =0; k < jarr.length(); k++)
			{
				//String str_alldata = jarr.getJSONObject(k).getString(key);
				//JSONArray newjarr = new JSONArray(str_alldata);
				for(int j = 0; j < jarr.length(); j++)
				{
					String name = jarr.getJSONObject(j).getString("name");
                    //String id   = jarr.getJSONObject(j).getString("E_ID");

                    //System.out.println(">>-- NAME  :   "+name);                        
                    //System.out.println(">>-- E_ID  :   "+id); 
				}
				//System.out.println(jarr.getJSONObject(k).getString("name"));
				currentMovie1Attribute.add(jarr.getJSONObject(k).getString("name"));
			}
			
			JSONArray jarr2 = new JSONArray(movie2Attributes.get(i));
			for(int k =0; k < jarr2.length(); k++)
			{
				//String str_alldata = jarr.getJSONObject(k).getString(key);
				//JSONArray newjarr = new JSONArray(str_alldata);
				for(int j = 0; j < jarr2.length(); j++)
				{
					String name = jarr2.getJSONObject(j).getString("name");
                    //String id   = jarr.getJSONObject(j).getString("E_ID");

                    //System.out.println(">>-- NAME  :   "+name);                        
                    //System.out.println(">>-- E_ID  :   "+id); 
				}
				//System.out.println(jarr2.getJSONObject(k).getString("name"));
				currentMovie2Attribute.add(jarr2.getJSONObject(k).getString("name"));
			}
			
			
////			JSONObject jObject = new JSONObject(keywordCol);
////			Iterator<?> keys=jObject.keys();
////			while(keys.hasNext())
////			{
////				String key = (String)keys.next();
////				System.out.println(key);
////			}
		    
		
		    
//		    JSONArray jarr = new JSONArray(movie1Attributes.get(i));
//		    currentMovie1Attribute
		    
		    int count = 0;
		    for(String currentAttribute : currentMovie2Attribute)
		    {
		    	if(currentMovie1Attribute.contains(currentAttribute))
		    	{
		    		count++;
	                //currentMovie1Attribute.remove(currentAttribute);
	                //currentMovie2Attribute.remove(currentAttribute);
	            }
		    }
		    distance = distance + currentMovie1Attribute.size() + currentMovie2Attribute.size() - (count*2);
		}
		//Random random = new Random();
		return distance;
		
	}
	
	public static SortedMap<Double,String> famous(ArrayList<String> returnList, int w)
	{
		
		Connection conn = null;
		Statement stmt = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			System.out.println("Connecting to database");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			
			System.out.println("Creating statement");
			stmt = conn.createStatement();
			
			String sql = "Select vote_average, vote_count, title From movies where title IN (";
			for(String x : returnList)
			{
				sql = sql +"'" + x +"',"; 
			}
			sql = sql.substring(0,sql.length()-1);
			sql = sql + ")";
			//System.out.println(sql);
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Double> voteAverage = new ArrayList<>();
			ArrayList<Integer> voteCount = new ArrayList<>();
			ArrayList<String> movieTitle = new ArrayList<>();
			
			while(rs.next())
			{
				voteCount.add(rs.getInt("vote_count"));
				voteAverage.add(rs.getDouble("vote_average"));
				movieTitle.add(rs.getString("title"));
			}
			int maxCount = Collections.max(voteCount);
			//System.out.println(maxCount);
			SortedMap<Double, String> moviePopMap = new TreeMap<Double, String>();
			double formula;
			for(int i = 0; i < voteAverage.size(); i++)
			{
				formula = ((voteAverage.get(i)*voteAverage.get(i))*voteCount.get(i))/maxCount;
				moviePopMap.put(formula,movieTitle.get(i));
			}
			SortedMap<Double, String> newMap = new TreeMap<>();
			List<Double> keys = new ArrayList<>(moviePopMap.keySet());
			for(int i = keys.size()-1;i>=keys.size()-5;i--)
			{
				newMap.put(keys.get(i),moviePopMap.get(keys.get(i)));
			}
			
			
//			SortedMap<Double, String> newMap = new TreeMap<>();
//			for(int i =moviePopMap.size(); i >=5 i--)
//			{
//				newMap.put(key, value)
//			}
			
			return newMap;
		
		}
		catch(Exception e)
		{
			System.out.println("connection fail");
			e.printStackTrace();
		}
		finally
		{
			try {
				if(stmt!=null)
					stmt.close();
			}
			catch(Exception e2)
			{}
			try {
				if(conn!=null)
					conn.close();
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		return null;
	}
	
}






















