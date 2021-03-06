package com.strongit.iss.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fr.third.org.hsqldb.lib.StringUtil;
import com.strongit.iss.common.Cache;
import com.strongit.iss.common.Constant;
import com.strongit.iss.common.MD5;
import com.strongit.iss.exception.BusinessServiceException;
import com.strongit.iss.service.BaseService;
import com.strongit.iss.service.IFivePlanService;

import edu.emory.mathcs.backport.java.util.Arrays;
/**
 * @author zhaochao
 * @date 2018-09-06
 */
@Service
@Transactional
public class FivePlanServiceImpl extends BaseService implements IFivePlanService{

    @Autowired
    private ReportCacheServiceImpl reportCacheService;
	
	@Override
	/**
	 * 五年项目储备地图报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	public List<Map<String, Object>> getFivePlanPlaceReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
        
        
		SQL.append(" select ");
		SQL.append(" SUBSTR(vinf.build_place, 0, @@@)||'####'  as \"itemCode\",  ");//-- 建设地点
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" where 1=1 ");//---建设地点
		//SQL.append(searchSql); //获取过滤条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		
		
	    String zeroNun="0000";
	    Integer num=2;
	    String filterSql="";
	    String code=filters.get(Constant.BUILD_PLACE_GROUPNO);
	    if(StringUtils.isNotBlank(code)){
		    if(Arrays.asList(Constant.ARRAY).contains(code)){
				// 下钻过滤
				if ("0000".equals(code.substring(2, 6))) {
					// 截取位数
			    	zeroNun="";
			    	num=6;
					SQL.append(" AND vinf.BUILD_PLACE like '" + code.substring(0, 2) + "%'");
				}// 展现第二级
				else if ("".equals(code.substring(6, 6))) {
					// 截取位数
			    	zeroNun="";
			    	num=6;
			    	filterSql=" AND vinf.BUILD_PLACE like '" +code.substring(0, 6)+"%'";
				}
		    }else{
			    // 展现第一级
			    if("0000".equals(code.substring(2, 6))){
			    	// 截取位数
			    	zeroNun="00";
			    	num=4;
			    	filterSql=" AND vinf.BUILD_PLACE like '" +code.substring(0,2)+"%'";
			    }// 展现第二级
			    else if("00".equals(code.substring(4, 6))){
			    	// 截取位数
			    	zeroNun="";
			    	num=6;
			    	//申报单位
			    	filterSql=" AND vinf.BUILD_PLACE like '" +code.substring(0,4)+"%'";	    	 
			    } // 展现第三级
			    else if("".equals(code.substring(6, 6))){
			    	// 截取位数
			    	zeroNun="";
			    	num=6;
			    	filterSql=" AND vinf.BUILD_PLACE like '" +code.substring(0,6)+"%'";
			    }
		    }
	    }
	    SQL.append(filterSql);
	    SQL.append(querySql);
        SQL.append(" GROUP BY SUBSTR(vinf.BUILD_PLACE,0,@@@)||'####'");
        SQL.append(" ORDER BY SUBSTR(vinf.BUILD_PLACE,0,@@@)||'####' asc ");
//		// 个数排序
//		if(Constant.ORDERBY_CNT.equals(filters.get(Constant.ORDERBY))){			
//		    SQL.append(" ORDER BY \"cnt\" desc, SUBSTR(vinf.BUILD_PLACE,0,@@@)||'####' ");
//		}
//		// 金额排序
//		else if(Constant.ORDERBY_MON.equals(filters.get(Constant.ORDERBY))){		
//			 SQL.append(" ORDER BY \"investMon1\" desc, SUBSTR(vinf.BUILD_PLACE,0,@@@)||'####' ");
//		}
//		//资金需求
//		else if("orderbyapply".equals(filters.get(Constant.ORDERBY))){
//			SQL.append(" ORDER BY \"applyinvestMon1\" desc, SUBSTR(vinf.BUILD_PLACE,0,@@@)||'####' ");
//		}
        String sql=SQL.toString().replaceAll("@@@", num.toString()).replaceAll("####", zeroNun);
		//调取缓存中的结果
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list  =this.dao.findBySql(sql, new Object[]{});
		for(int i=0;i<list.size();i++){
			if(null==(list.get(i).get("itemCode"))||list.get(i).get("itemCode").equals("")){
				list.get(i).put("itemName", "其他");
			}else if("99".equals(list.get(i).get("itemCode").toString().substring(0, 2))){
				list.get(i).put("itemName", "跨省区");
			}else{
				list.get(i).put("itemName", Cache.getNameByCode("1",(String)list.get(i).get("itemCode")));
			}
		}
		long endMilis=System.currentTimeMillis();
		logger.debug("OverViewServiceImpl getFivePlanPlaceReportByMap 方法执行查询花费毫秒数" + (endMilis-startMilis));
		return list;
	}

	
	/**
	 * 五年项目储备国标行业报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanGbindustryReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//参数的序号表示分相应的级别显示
		String kkk=filters.get("GBIndustryLevel");
		//需要查询的维度级别
		String proStageStr = " vinf.GB_INDUSTRY ";//-- 国标行业
		//获取分国标行业统计项目信息
		if(kkk==null){
			//获取第一级item_full_key
			proStageStr="substr(di1.item_full_key, 1, instr(di1.item_full_key||'-', '-', 1, 1)-1)   ";
		}else if("1".equals(kkk)){
			//获取第二级
			proStageStr="substr(di1.item_full_key, instr(di1.item_full_key||'-', '-', 1, 1)+1, instr(di1.item_full_key||'-', '-', 1, 2)-instr(di1.item_full_key||'-', '-', 1, 1)-1) ";
		}else if("2".equals(kkk)){
			//获取第三级
			proStageStr="substr(di1.item_full_key, instr(di1.item_full_key||'-', '-', 1, 2)+1, instr(di1.item_full_key||'-', '-', 1, 3)-instr(di1.item_full_key||'-', '-', 1, 2)-1) ";
		}else{
			//获取第四级
			
		}
		
		
		//获取分国标行业统计项目信息
		SQL.append(" select  ");
		SQL.append(proStageStr+" as \"itemCode\", ");//-- 国标行业
		SQL.append(" count(vinf.ID) as \"cnt\",  ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\"  ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" left join dictionary_items di1 on vinf.GB_INDUSTRY = di1.item_key and di1.group_no='2' ");
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by "+proStageStr);//-- 国标行业
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		
		String sql=SQL.toString();
        //sql = CommonUtils.formatYearApplyCaptionCloumn(filters.get("currentYear"),sql);
		//调取缓存中的结果
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		for(int i=0;i<list.size();i++){
			if(null==(list.get(i).get("itemCode"))||list.get(i).get("itemCode").equals("")){
				list.get(i).put("itemName", "其他");
			}else{
				list.get(i).put("itemName", Cache.getNameByCode("2",(String)list.get(i).get("itemCode")));
			}
		}
		
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanGbindustryReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		return list;
	
	}

	
	/**
	 * 五年项目储备所属行业报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanIndustryReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//参数的序号表示分相应的级别显示
		String kkk=filters.get("IndustryLevel");
		//需要查询的维度级别
		String proStageStr = " vinf.INDUSTRY ";//-- 所属行业
		//获取分所属行业统计项目信息
		if(kkk==null){
			//获取第一级item_full_key
			proStageStr="substr(di1.item_full_key, 1, instr(di1.item_full_key||'-', '-', 1, 1)-1)   ";
		}else{
			//获取第二级
			
		}
		SQL.append(" select  "); 
		SQL.append(proStageStr + " as \"itemCode\", "); //-- 所属行业
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\"  ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" left join dictionary_items di1 on vinf.INDUSTRY = di1.item_key and di1.group_no='8' ");
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by "+proStageStr); //-- 所属行业
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		       
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		for(int i=0;i<list.size();i++){
			if(null==(list.get(i).get("itemCode"))||list.get(i).get("itemCode").equals("")){
				list.get(i).put("itemName", "其他");
			}else{
				list.get(i).put("itemName", Cache.getNameByCode("8",(String)list.get(i).get("itemCode")));
			}
		}
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanIndustryReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	
	/**
	 * 五年项目储备重大战略报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanMajorstrategyReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//获取分重大战略统计项目信息
		SQL.append(" select  ");//
		SQL.append(" vinf.MAJOR_STRATEGY as \"itemCode\", ");//---符合重大战略
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by    vinf.MAJOR_STRATEGY  ");// ---符合重大战略
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		for(int i=0;i<list.size();i++){
			if(null==(list.get(i).get("itemCode"))||list.get(i).get("itemCode").equals("")){
				list.get(i).put("itemName", "其他");
			}else{
				list.get(i).put("itemName", Cache.getNameByCode("14",(String)list.get(i).get("itemCode")));
			}
		}
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanMajorstrategyReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	
	/**
	 * 五年项目储备申报部门报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanCreatedepartmentguidReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//参数的序号表示分相应的级别显示
		String kkk="";
		if(StringUtil.isEmpty(filters.get("RecororderbySqlptCode"))){
			kkk="1";
		}//展现二级
		else{
			kkk="2";
		}		
		//需要查询的维度级别
		String proStageStr = "";
		String proStageStr1 = "";
		//获取分国标行业统计项目信息
		if("1".equals(kkk)){
			//获取第一级
			//申报部门类型（地方发改委、中央部门、央企）
			proStageStr=" CASE WHEN substr(di2.department_full_type, 1, instr(di2.department_full_type ||'#', '#', 1, 1)-1)='FGW' THEN '地方发改委' "
					+ "WHEN substr(di2.department_full_type, 1, instr(di2.department_full_type ||'#', '#', 1, 1)-1)='DEPT' THEN '中央部门' "
					+ "when substr(di2.department_full_type, 1, instr(di2.department_full_type ||'#', '#', 1, 1)-1)='CENTRE-COM' then '央企'  END ";
			proStageStr1=" substr(di2.department_full_type, 1, instr(di2.department_full_type ||'#', '#', 1, 1)-1) ";
		}else{
			//获取第二级
			proStageStr=" substr(di2.department_full_codename, 1, instr(di2.department_full_codename ||'#', '#', 1, 1)-1)  ";//--  申报部门
			proStageStr1=" substr(di2.department_full_codename, 1, instr(di2.department_full_codename ||'#', '#', 1, 1)-1)  ";
		}
		
		//获取分申报部门统计项目信息as \"itemCode\",
		SQL.append(" select  "); 
		SQL.append(proStageStr + "  as \"itemName\", ");// ---申请部门ID
		SQL.append(" count(vinf.ID) as \"cnt\", ");// ---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");// ---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");// ---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" left join department di2  on  vinf.CREATE_DEPARTMENT_GUID = di2.department_guid  ");//-- 申报部门
		SQL.append(" where substr(di2.department_full_type, 1, instr(di2.department_full_type ||'#', '#', 1, 1)-1) in('FGW', 'DEPT','CENTRE-COM') ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		
		if("地方发改委".equals(filters.get("RecordDdptCode"))){
			SQL.append(" and  vinf.department_full_type = 'FGW' ");
		}else if("央企".equals(filters.get("RecordDdptCode"))){
			SQL.append(" and  vinf.department_full_type = 'CENTRE-COM' ");
		}else if("中央部门".equals(filters.get("RecordDdptCode"))){
			SQL.append(" and  vinf.department_full_type = 'DEPT' ");
		}
		
		//{filterStatus=undefined, RecordDdptCode=地方发改委, RecororderbySqlptCode=1}
		
		SQL.append(" group by  " + proStageStr1);// ---申请部门ID
	
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
//		for(int i=0;i<list.size();i++){
//			if(null==(list.get(i).get("itemCode"))){
//				list.get(i).put("itemName", "其他");
//			}else{
//				list.get(i).put("itemName", Cache.getNameByCode("1",(String)list.get(i).get("itemCode")));
//			}
//		}
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanCreatedepartmentguidReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	
	/**
	 * 五年项目储备储备情况报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanStorelevelReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//获取分储备情况统计项目信息
		SQL.append(" select  ");//
		SQL.append(" CASE WHEN vinf.STORE_LEVEL = '1'  THEN '县级' WHEN vinf.STORE_LEVEL = '2' THEN '市级' WHEN vinf.STORE_LEVEL = '3' THEN '省级' WHEN vinf.STORE_LEVEL = '4' THEN '国家' END as \"itemName\", ");//
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by   vinf.STORE_LEVEL  ");// ---储备级别
	
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		
		
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanStorelevelReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	
	/**
	 * 五年项目储备入库时间报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanStoretimeReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//获取分入库时间统计项目信息
		SQL.append(" select  case when to_char(vinf.STORE_TIME, 'yyyy-MM') is null then '其他' else ");//
		SQL.append(" to_char(vinf.STORE_TIME,'yyyy-MM') end as \"itemName\", ");//---入库时间
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by  to_char(vinf.STORE_TIME,'yyyy-MM') ");//---入库时间
		
		//根据前端传来的参数判断按相应的字段排序
		SQL.append(" order by to_char(vinf.STORE_TIME,'yyyy-MM') asc  ");
//		if("0".equals(orderbySql)){
//			SQL.append(" order by count(vinf.ID) desc  ");
//		}else{
//			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
//		}
		
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanStoretimeReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		//reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	
	/**
	 * 五年项目储备项目类型报表数据
	 * @orderBy 
	 * @param filters
	 * @return
	 * @throws BusinessServiceException
	 * @author zhoupeng
	 * @Date 2016年10月22日下午2:56:15
	 */
	@Override
	public List<Map<String, Object>> getFivePlanProtypeReportByMap(
			Map<String, String> filters,String searchSql,String querySql,String orderbySql) throws BusinessServiceException {
		StringBuilder SQL = new StringBuilder();
		//获取分项目类型统计项目信息
		SQL.append(" select  ");//
		SQL.append(" vinf.PRO_TYPE as \"itemCode\", ");//  ---项目类型
		SQL.append(" count(vinf.ID) as \"cnt\", ");//---项目ID
		SQL.append(" sum(vinf.INVESTMENT_TOTAL)/10000 as  \"investMon1\" ");//---总投资（亿元）
		SQL.append(" from v_five_plan_child vinf  ");//---where vinf.IS_PLAN is null ---是否编制三年滚动计划
		SQL.append(" where 1=1 ");
		SQL.append(searchSql); //获取过滤条件
		SQL.append(querySql);//外部查询条件
		//是否编制计划：1-是，0-否
		if("unfinish".equals(filters.get("filterStatus"))){
			SQL.append(" and  vinf.IS_PLAN = '0' ");
		}else{
			
		}
		SQL.append(" group by    vinf.PRO_TYPE   ");//---项目类型
		//根据前端传来的参数判断按相应的字段排序
		if("0".equals(orderbySql)){
			SQL.append(" order by count(vinf.ID) desc  ");
		}else{
			SQL.append(" order by sum(vinf.INVESTMENT_TOTAL) desc ");
		}
		
		
		String sql=SQL.toString();
		long startMilis=System.currentTimeMillis();
		//执行查询并将结果转化为ListMap
		List<Map<String,Object>> list =this.dao.findBySql(sql, new Object[]{});
		for(int i=0;i<list.size();i++){
			if(null==(list.get(i).get("itemCode"))||list.get(i).get("itemCode").equals("")){
				list.get(i).put("itemName", "谋划阶段");
			}else{
				list.get(i).put("itemName", Cache.getNameByCode("3",(String)list.get(i).get("itemCode")));
			}
		}
		long endMilis=System.currentTimeMillis();
		logger.debug("RollPlanServiceImpl getFivePlanProtypeReportByMap 方法执行查询花费毫秒数:" + (endMilis-startMilis));
		//将数据存入缓存中
		reportCacheService.putEverObject(MD5.encode(SQL.toString()), list);
		return list;
	}

	


}
