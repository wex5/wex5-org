package org;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.justep.baas.action.ActionContext;

public class LoadPerson {
	private static final String DATASOURCE_X5SYS = "system";

	public static JSONObject loadPerson(JSONObject params, ActionContext context) throws SQLException, NamingException {
		String sPersonID = params.getString("sPersonID");
		Connection conn = context.getConnection(DATASOURCE_X5SYS);
		JSONObject ret = new JSONObject();
		Statement stmt = null;
		String sql = " select so.sID,so.sName as " + "sChineseFirstPY,sp.sPhoto,so.sCode,so.sFCode,so.sFID,so.sFName,so.sName,sp.sNumb,"
				+ "so.sOrgKindID,so.sPersonID,sp.sPhotoLastModified,so.sSequence,so.sValidState" + " from sa_oporg so inner join sa_opperson sp on so.sPersonID=sp.sID"
				+ " where substring_index(so.sFID,'/',2) in" + "(select  distinct substring_index(o.sFID,'/',2) as ognFID from sa_oporg o where o.sPersonID = '" + sPersonID + "')"
				+ "and so.sOrgKindID='psm'";
		stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		JSONArray jArray = new JSONArray();
		while (result.next()) {
			JSONObject json = new JSONObject();
			json.put("sID", result.getString("sID"));
			json.put("sChineseFirstPY", result.getString("sChineseFirstPY"));
			json.put("sFID", result.getString("sFID"));
			json.put("sCode", result.getString("sCode"));
			json.put("sFCode", result.getString("sFCode"));
			json.put("sFName", result.getString("sFName"));
			json.put("sName", result.getString("sName"));
			json.put("sNumb", result.getLong("sNumb"));
			json.put("sOrgKindID", result.getString("sOrgKindID"));
			json.put("sPersonID", result.getString("sPersonID"));
			json.put("sPhoto", result.getString("sPhoto"));
			if(result.getTimestamp("sPhotoLastModified")==null){
				json.put("sPhotoLastModified", "");
			}else{
				json.put("sPhotoLastModified", result.getTimestamp("sPhotoLastModified").toString());
			}
			json.put("sSequence", result.getString("sSequence"));
			json.put("sValidState", result.getInt("sValidState"));
			jArray.add(json);
		}
		ret.put("persons", jArray);
		if (stmt != null)
			stmt.close();
		if (conn != null)
			conn.close();
		return ret;
	}

	public static JSONObject getDepts(JSONObject params, ActionContext context) throws SQLException, NamingException {
		String sPersonID = params.getString("sPersonID");
		Connection conn = context.getConnection(DATASOURCE_X5SYS);
		JSONObject ret = new JSONObject();
		Statement stmt = null;
		String sql ="select so.sID,so.sName as sChineseFirstPY,so.sCode,so.sFCode,"+
							"so.sFID,so.sFName,so.sName,so.sOrgKindID,so.sSequence,"+
							"so.sValidState,so.sPersonID from sa_oporg so"+ 
							" where substring_index(so.sFID,'/',2) in ( select substring_index(o.sFID,'/',2) as ognFID"+
							" from sa_oporg o where o.sPersonID = '"+sPersonID+"')"+ 
					        "and (so.sOrgKindID='dpt' or so.sOrgKindID='ogn')";
		stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery(sql);
		JSONArray jArray = new JSONArray();
		while (result.next()) {
			JSONObject json = new JSONObject();
			json.put("sID", result.getString("sID"));
			json.put("sChineseFirstPY", result.getString("sChineseFirstPY"));
			json.put("sCode", result.getString("sCode"));
			json.put("sFCode", result.getString("sFCode"));
			json.put("sFID", result.getString("sFID"));
			json.put("sFName", result.getString("sFName"));
			json.put("sName", result.getString("sName"));
			json.put("sOrgKindID", result.getString("sOrgKindID"));
			json.put("sSequence", result.getString("sSequence"));
			json.put("sValidState", result.getString("sValidState"));
			json.put("sPersonID", result.getString("sPersonID"));
			jArray.add(json);
		}
		ret.put("depts", jArray);
		if (stmt != null)
			stmt.close();
		if (conn != null)
			conn.close();
		return ret;
	}
}
