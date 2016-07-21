package org;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.action.ActionContext;

public class UpdatePersonUid {
	private static final String DATASOURCE_X5SYS = "system";

	public static JSONObject updatePersonUid(JSONObject params, ActionContext context) throws SQLException, NamingException {
		Connection conn = context.getConnection(DATASOURCE_X5SYS);
		Statement stmt = null;
		JSONObject ret = new JSONObject();
		String pid = params.getString("pid");
		int uid =Integer.parseInt(params.getString("uid") == null ? "0" : params.getString("uid").toString()); ;
		System.out.print(uid);
		String sql = "update sa_opperson set sNumb=" + uid + " where sID= '"+pid+"'" ;
		stmt = conn.createStatement();
		int i = stmt.executeUpdate(sql);
		System.out.print(i);
		if(i>0){
			ret.put("state", true);
			if(stmt!=null)
				stmt.close();
			if(conn!=null)
				conn.close();
		}else{
			ret.put("state", false);
			if(stmt!=null)
				stmt.close();
			if(conn!=null)
				conn.close();
		}
		return ret;
	}
	public static JSONObject personAvatar(JSONObject params, ActionContext context) throws SQLException, NamingException {
		return null;
	}
}
