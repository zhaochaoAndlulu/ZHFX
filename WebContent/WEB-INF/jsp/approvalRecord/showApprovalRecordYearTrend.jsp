<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>综合分析</title>
    <link rel="stylesheet" type="text/css" href="${ctx}/themes/css/iframe.css" />
    <link rel="stylesheet" type="text/css" href="${ctx}/themes/css/layout.css" />
    <script type="text/javascript" src="${ctx}/js/jquery-1.11.3.min.js"></script>
    <script src="${ctx}/js/base.js"></script>
    <script src="${ctx}/js/echarts/echarts-all.js"></script>
    <script type="text/javascript" src="${ctx}/common/extend.js"></script>
</head>
<body>
<div style="margin-left:10px;">
<button class="typeCls select-on" value="">全部</button><button class="typeCls" value="A00001">审批
</button><button class="typeCls" value="A00002">核准
</button><button class="typeCls" value="A00003">备案</button>
</div>
	<div id="fhyhz" class="chartDiv divContent" style="top:40px">
        <span class="jbg topLeft"></span>
        <span class="jbg topRight"></span>
        <span class="jbg bottomLeft"></span>
        <span class="jbg bottomRight"></span>
        <!--  中间的内容区域 -->
        <h6 class="chartDivTitle">
	        <span class="r">
		        <a href="javascript:void(0)" onclick="Tab(this,'.chartDivTitle a','click-on');showHide('#fhy-gs');switchTitleTxt('gs')" class="click-on">
		       		 个数
		        </a>
		        <a href="javascript:void(0)" onclick="Tab(this,'.chartDivTitle a','click-on');showHide('#fhy-ztz');switchTitleTxt('tz')" >
		       		 总投资
		        </a>
	        </span>
           <span  id="titleTxt"> 审核备项目投资同比</span>
        	<i title="设为主控"></i>        	
<!--         	<a class="more" href="javaScript:void(0)">更多>></a> -->
        </h6>
        <div class="chartHeight" id="fhy-gs" style="visibility: hidden;">
            <div class="chartCenter" id="myNumChart" > </div>
        </div>
        <div class="chartHeight" id="fhy-ztz">
            <div class="chartCenter" id="myChart"> </div>
        </div>
    </div>
    <script type="text/javascript">
  		//定义父窗口
    	var parentWin = parent;
    	var orderby="orderbymon";
  		//页面初始化加载
    	window.onload = function(){
        	for (var i=0;i<5;i++) {
            	if (parentWin.win) {
            		//将当前窗口定义到父级窗口
                	parentWin.win["viewYearTrends"] = window;
            	}else{
                	parentWin = parentWin.parent;
            	}
        	}
     		// 装载数据 
        	loadData();
     		
            //单击更多触发事件
         	$('.chartDivTitle').find(".more").bind("click",function(){
         		var url = "${ctx}/view/overView!getFivePlanInfoTrends?orderby="+orderby;
         		var name=parentWin.filters["params.moduleName"];
                 var params =parentWin.filters;
         		loadMoreData(url,params);
         	});
            
         	//审核备类型
            $(".typeCls").bind("click",function(){
            	var AuditPreparationCode = $(this).val();
            	parentWin.filters["params.AuditPreparationCode"]=AuditPreparationCode;
                $(this).siblings(".typeCls").removeClass("select-on");
                $(this).addClass("select-on");
            	parentWin.refreshWin();
         	});
            
            //个数
            $(".r a:eq(0)").bind("click",function(){
            	Tab(this,'.chartDivTitle a','click-on');
            	showHide('#fzf-gs');
            	// 项目个数 
            	orderby="orderbycnt";
         		loadData();
         	});
            // 总投资
            $(".r a:eq(1)").bind("click",function(){
            	Tab(this,'.chartDivTitle a','click-on');
            	 showHide('#fzf-ztz');
            	// 总投资  
             	orderby="orderbymon";
          		loadData();
         	});
    	}

    	/***
    	 *  切换标题
    	 * @orderBy
    	 * @param
    	 * @return
    	 * @author tannc
    	 * @Date 2016/11/5 13:36
    	 **/
    	function  switchTitleTxt(name){
    	    switch (name){
                case "gs": $("#titleTxt").text("审核备项目个数同比"); break;
                case "tz": $("#titleTxt").text("审核备项目投资同比")
            }
        }
  		var option = {
  			    tooltip : {
  			        trigger: 'axis',
                    textStyle:ZHFX.tooltipTextStyle,
  			      	formatter : function(params) {
			        	var str = '';
			        	if (null != params && params.length > 0) {
			        		if ('01月' == params[0].name) {
			        			str += params[0].name + '</br>';
			        		} else {
				        		str += '01月-' + params[0].name + '</br>';
			        		}
			        		for (var i = 0; i < params.length; i++) {
			        			if (params[i].seriesName.indexOf('柱') > -1) {
				        			str += params[i].seriesName + '审核备项目总投资：' + formatAmount(params[i].value) + '</br>';
			        			} else {
			        				str += params[i].seriesName + '同比增长：' 
			        					+ ('-' != params[i].value ? params[i].value + '%' : params[i].value) + '</br>';
			        			}
			        		}
			        	}
			        	return str;
			        }
  			    },
  			    legend: {
                    textStyle:ZHFX.legendTextStyle,
                    selected: {
                        '2014年(柱)' : false,
                        '2015年(柱)' : false,
                        '2015年' : false
                    },
  			        data:['2014年(柱)','2015年(柱)','2016年(柱)','2015年','2016年']
  			    },
                 grid:{
                        borderWidth:0,
                        x: 100,
                        x2:100,
                        y: 60,
                        y2:40
                    },
  			    calculable :false,
  			    xAxis : [
                    {
                        type: 'category',
                        axisLine:ZHFX.axisLine,
                        splitLine: {show:false},
                        data: [],
                        axisLabel: {
                            rotate:'-15',
                            formatter:function(value){
                                if ('01月'!= value) {
                                    value= '01月-' + value ;
                                }
                                return value;
                            },
                            textStyle: ZHFX.XTextStyle
                        }
                    }
  			    ],
  			    yAxis : [
  			        {
  			            type : 'value',
  			          	name : '总投资(柱)',
			          	'nameTextStyle' : {
							color : '#ff7f50'	
						},
                        splitLine:{
                            show:true,
                            lineStyle: {
                                color: '#333e50',
                                type:'dashed'

                            }
                        },
  			            axisLabel : {
                            textStyle: ZHFX.YTextStyle,
  			            	formatter : function(value) {
		                    	if ((formatAmount(value)+'').indexOf('.00') > 0) {
	                         		return (formatAmount(value)+'').replace('.00','');
	                         	} else if ('0元' != formatAmount(value)) {
	                          		return formatAmount(value);
	                          	} else {
	                         		return 0;
	                         	}
		                    }
  			            }
  			        },
  			      	{
    					'type':'value',
    					'name':'增速（折线）',
    					'nameTextStyle' : {
    						color : '#87cefa'	
    					},
    					axisLabel: {
                            textStyle: ZHFX.YTextStyle,
                            formatter : function(value) {
                                return value + '%';
                            }
                        },
                        splitLine:{
                            show:false,
                            lineStyle: {
                                color: '#333e50',
                                type:'dashed'

                            }
                        }
    				}
  			    ],
  			    series : [
					{
					    name:'2014年(柱)',
					    type:'bar',
					    data:[]
					},
					{
					    name:'2015年(柱)',
					    type:'bar',
					    data:[]
					},
					{
					    name:'2016年(柱)',
					    type:'bar',
					    data:[],
					    itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				//return formatFund(params.value);
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
					},
  			      	{
  			            name:'2015年',
  			          	'yAxisIndex':1,
  			            type:'line',
  			            data:[],
  			          	itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				return params.value;
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
  			        },
  			      	{
  			            name:'2016年',
  			          	'yAxisIndex':1,
  			            type:'line',
  			            data:[],
  			          	itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				return params.value;
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
  			        }
  			    ]
  			};
  		
  		var optionNum = {
  			    tooltip : {
  			        trigger: 'axis',
                    textStyle:ZHFX.tooltipTextStyle,
  			      	formatter : function(params) {
			        	var str = '';
			        	if (null != params && params.length > 0) {
			        		if ('01月' == params[0].name) {
			        			str += params[0].name + '</br>';
			        		} else {
				        		str += '01月-' + params[0].name + '</br>';
			        		}
			        		for (var i = 0; i < params.length; i++) {
			        			if (params[i].seriesName.indexOf('柱') > -1) {
				        			str += params[i].seriesName + '审核备项目个数：' + formatCount(params[i].value) + '</br>';
			        			} else {
			        				str += params[i].seriesName + '同比增长：' 
			        					+ ('-' != params[i].value ? params[i].value + '%' : params[i].value) + '</br>';
			        			}
			        		}
			        	}
			        	return str;
			        }
  			    },
  			    legend: {
                    textStyle:ZHFX.legendTextStyle,
                    selected: {
                        '2014年(柱)' : false,
                        '2015年(柱)' : false,
                        '2015年' : false
                    },
  			        data:['2014年(柱)','2015年(柱)','2016年(柱)','2015年','2016年']
  			    },
                grid:{
                    borderWidth:0,
                    x: 100,
                    x2:100,
                    y: 60,
                    y2:40
                },
  			    calculable : false,
  			    xAxis : [
                    {
                        type: 'category',
                        splitLine: {show:false},
                        data: [],
                        axisLabel: {
                            rotate:'-15',
                            formatter:function(value){
                                if ('01月'!= value) {
                                    value= '01月-' + value ;
                                }
                                return value;
                            },
                            textStyle: ZHFX.XTextStyle
                        },
                        axisLine:ZHFX.axisLine
                    }
  			    ],
  			    yAxis : [
  			        {
  			            type : 'value',
  			         	 name : '个数(柱)',
			          	'nameTextStyle' : {
							color : '#ff7f50'	
						},
                        splitLine:{
                            show:true,
                            lineStyle: {
                                color: '#333e50',
                                type:'dashed'

                            }
                        },
  			            axisLabel : {
                            textStyle: ZHFX.YTextStyle,
                            formatter : function(value) {
		                    	if ('0个' != formatCount(value)) {
	                          		return formatCount(value);
	                          	} else {
	                         		return 0;
	                         	}
		                    }
  			            }
  			        },
  			      	{
    					'type':'value',
    					'name':'增速（折线）',
    					'nameTextStyle' : {
    						color : '#87cefa'	
    					},
    					axisLabel: {
                            textStyle: ZHFX.YTextStyle,
                            formatter : function(value) {
                                return value + '%';
                            }
                        },
                        splitLine:{
                            show:false,
                            lineStyle: {
                                color: '#333e50',
                                type:'dashed'

                            }
                        }
    				}	
  			    ],
  			    series : [
					{
					    name:'2014年(柱)',
					    type:'bar',
					    data:[]
					},
					{
					    name:'2015年(柱)',
					    type:'bar',
					    data:[]
					},
					{
					    name:'2016年(柱)',
					    type:'bar',
					    data:[],
					    itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				//return formatCnt(params.value);
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
					},
  			      	{
  			            name:'2015年',
  			          	'yAxisIndex':1,
  			            type:'line',
  			            data:[],
  			          	itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				return params.value;
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
  			        },
  			      	{
  			            name:'2016年',
  			          	'yAxisIndex':1,
  			            type:'line',
  			            data:[],
  			          	itemStyle: {
			            	normal: {
			            		label : {
			            			show : true,
			            			position : 'top',
			            			formatter: function (params) {
			            				return params.value;
			                        },
			                        textStyle : ZHFX.topTextStyle
			                    }
			            	}
			            }
  			        }
  			    ]
  			};
  		
    	//初始化数据
    	var myChart = echarts.init(document.getElementById('myChart'));
    	var myNumChart = echarts.init(document.getElementById('myNumChart'));
    	
    	/**
    	 * 加载数据
    	 */
    	function loadData() {

    		myChart.showLoading({
                text: '正在努力的加载数据中...'
            });
    		var params = parentWin.filters;
    		// 异步加载 全国项目进展情况
            $.ajax({
                url: "${ctx}/view/overView!getAuditPreparationTrends",
                data : params,
                dataType: "json",
                success: function (data) {
                    //清空数据
                    option.xAxis[0].data = [];
                    //设置数据
                    option.series[0].data = [];
                    option.series[1].data = [];
                    option.series[2].data = [];
                    option.series[3].data = [];
                    option.series[4].data = [];
                    //总投资
                    optionNum.xAxis[0].data = [];
                    //设置数据
                    optionNum.series[0].data = [];
                    optionNum.series[1].data = [];
                    optionNum.series[2].data = [];
                    optionNum.series[3].data = [];
                    optionNum.series[4].data = [];

                	var legendAxisData = [];
                    var investMon = [];
                    var cnt = [];
                    var investMon1 = [];
                    var cnt1 = [];
                    var investMon2 = [];
                    var cnt2 = [];
                    var names = [];
                    var invTrend = [];
                    var cntTrend = [];
                    var invTrend1 = [];
                    var cntTrend1 = [];
                	if (data) {
                	    var  totalMon = 0;
                	    var  totalCnt = 0;
                	    var  totalMon1 = 0;
                	    var  totalCnt1 = 0;
                	    var  totalMon2 = 0;
                	    var  totalCnt2 = 0;
                		// 遍历对象
                        for (var i=0,len=data.length; i<len; i++) {
                        	var date = data[i]["itemName"];
                        	if (date.indexOf('2014') > -1) {
                        		totalMon = totalMon + parseInt(data[i]["investMon"]);
                   				totalCnt = totalCnt + parseInt(data[i]["cnt"]);
                   				investMon.push(totalMon);
                       			cnt.push(totalCnt);
                   				legendAxisData.push(date.substring(4,6) + '月');
                        	}
                        	if (date.indexOf('2015') > -1) {
                        		totalMon1 = totalMon1 + parseInt(data[i]["investMon"]);
                   				totalCnt1 = totalCnt1 + parseInt(data[i]["cnt"]);
                   				investMon1.push(totalMon1);
                       			cnt1.push(totalCnt1); 
                        	}
                        	if (date.indexOf('2016') > -1) {
                        		totalMon2 = totalMon2 + parseInt(data[i]["investMon"]);
                   				totalCnt2 = totalCnt2 + parseInt(data[i]["cnt"]);
                   				investMon2.push(totalMon2);
                       			cnt2.push(totalCnt2); 
                        	}
                        }
                		//2015同比增长
                		if (investMon1.length <= investMon.length) {
                			for (var i = 0; i < investMon1.length; i++) {
                				var trend = Number((Number(investMon1[i]) - Number(investMon[i])) * 100/Number(investMon[i])).toFixed(2);
                				var cntTr = Number((Number(cnt1[i]) - Number(cnt[i])) * 100/Number(cnt[i])).toFixed(2);
                				invTrend.push(trend);
                				cntTrend.push(cntTr);
                			}
                		}
                		//2016同比增长
                		if (investMon2.length <= investMon1.length) {
                			for (var i = 0; i < investMon2.length; i++) {
                				var trend = Number((Number(investMon2[i]) - Number(investMon1[i])) * 100/Number(investMon1[i])).toFixed(2);
                				var cntTr = Number((Number(cnt2[i]) - Number(cnt1[i])) * 100/Number(cnt1[i])).toFixed(2);
                				invTrend1.push(trend);
                				cntTrend1.push(cntTr);
                			}
                		}
                    }
                	option.xAxis[0].data = legendAxisData;
                	//设置数据
                    option.series[0].data = investMon;
                    option.series[1].data = investMon1;
                    option.series[2].data = investMon2;
                    option.series[3].data = invTrend;
                    option.series[4].data = invTrend1;
                    //总投资
                    optionNum.xAxis[0].data = legendAxisData;
                	//设置数据
                    optionNum.series[0].data = cnt;
                    optionNum.series[1].data = cnt1;
                    optionNum.series[2].data = cnt2;
                    optionNum.series[3].data = cntTrend;
                    optionNum.series[4].data = cntTrend1;
                    //加载隐藏
                    myChart.hideLoading();
                    //渲染对象
                    myChart.setOption(option,true);
                    myNumChart.setOption(optionNum,true);
                }
            });
    	}
    	
    	//刷新
        function refreshWin() {
            // 联动设置
            loadData();
        }
    </script>
</body>
</html>