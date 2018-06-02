import java.util.ArrayList;
import java.util.List;

public class JSON {
	
	private List<String> obj=new ArrayList<>();
	
	public void putInt(String key,int val){
		obj.add("\""+key+"\":"+"\""+val+"\"");
	}
	
	public void putLong(String key,long val){
		obj.add("\""+key+"\":"+"\""+val+"\"");
	}
	
	public void putFloat(String key,float val){
		obj.add("\""+key+"\":"+"\""+val+"\"");
	}
	
	public void putDouble(String key,double val){
		obj.add("\""+key+"\":"+"\""+val+"\"");
	}
	
	void putString(String key, String val){
		obj.add("\""+key+"\":"+"\""+val+"\"");
	}

	@Override
	public String toString()
	{
		if(obj.size()==0)return null;
		StringBuilder json;
		json = new StringBuilder("{");
		for(int i=0;i<obj.size()-1;i++){
			json.append(obj.get(i)).append(",");
		}
		json.append(obj.get(obj.size() - 1)).append("}");
		return json.toString();
	}
}