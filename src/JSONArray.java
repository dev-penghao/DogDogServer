import java.util.ArrayList;
import java.util.List;

public class JSONArray {
	
	private List<JSON> obj=new ArrayList<>();
	
	void putJson(JSON json){
		obj.add(json);
	}
	
	@Override
	public String toString()
	{
		if (obj.size()==0) return null;
		StringBuilder jsonArray;
		jsonArray = new StringBuilder("[");
		for(int i=0;i<obj.size()-1;i++){
			jsonArray.append(obj.get(i).toString()).append(",");
		}
		jsonArray.append(obj.get(obj.size() - 1)).append("]");
		return jsonArray.toString();
	}
}