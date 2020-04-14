
### 工资条接口整理
 - 为方便查看，有填充数据的为是，未填充数据的默认为否  
 
路径                  							|请求方式 |接口描述                 	|参与验签   	|是否测试	|是否白名单	|备注   
:---                  							|---      |---                        	|--- 			|---		|---		|---
/advertising/rotation 							|Get      |轮播图查询                 	|是           	|是			|			|
/manager/managerInfo 							|Get      |查询客户经理信息				|           	|			|			|
/manager/distribute								|Post     |通知企业分配客户经理    		|           	|			|			|
/manager/openingTips							|Get      |客户经理入口，弹窗提示     	|是           	|是			|			|
/inside/sendCode								|Post     |发送短信验证码             	|是           	|是			|			|
/inside/receipt									|Post     |员工回执             		|是           	|是			|			|
/inside/read									|Post     |已读工资条             		|是           	|是			|			|
/inside/bindWX									|Post     |微信号绑定身份证号         	|是           	|是			|			|
/inside/rz										|Post     |微信号绑定身份证号,无手机号	|是           	|是			|			|
/inside/setPwd									|Post     |设置密码             		|是           	|是			|			|
/inside/updPwd									|Post     |修改查询密码             	|           	|			|			|
/inside/checkPhoneCode							|Post     |验证手机验证码             	|是           	|是			|			|
/inside/updPhone								|Post     |修改手机号             		|是           	|是			|			|
/inside/updBankCard								|Post     |修改银行卡             		|是           	|是			|			|
/merchant/getAccess								|Post     |振兴访问凭证             	|           	|是			|			|未走网关，接口正常
/merchant/callback								|Get      |振兴回调             		|           	|是			|			|***有问题，需增加白名单
/mini/miniUserInfo								|Get      |小程序根据code获取用户信息	|           	|			|			|todo待确定是否删除
/msgmode										|Get      |查询账户信息					|           	|			|			|todo待确定是否删除
/roll/index										|Get      |首页             			|是         	|是			|			|
/roll/groupList									|Get      |企业机构列表             	|是           	|是			|			|
/roll/entEmp									|Get      |根据身份号返手机和公司列表 	|是           	|是			|			|
/roll/wageList									|Get      |个人薪资列表             	|是           	|是			|			|
/roll/wageDetail								|Get      |查看工资条详情    			|是           	|是			|			|
/roll/empInfo									|Get      |员工个人信息      			|           	|			|			|
/roll/invoice									|Get      |查询发票信息列表             |是           	|是			|			|
/roll/checkPwd									|Get      |验证密码             		|是           	|是			|			|
/roll/checkCard									|Get      |验证银行卡后六位  			|是           	|是			|			|
/roll/emp										|Get      |员工个人信息      			|是         	|是			|			|
/roll/empEnt									|Get      |员工企业             		|是           	|是			|			|
/roll/empCard									|Get      |员工银行卡            		|是           	|是			|			|
/roll/empCardLog								|Get      |员工银行卡修改记录     		|是           	|是			|			|
/roll/entPhone									|Get      |手机号和公司列表             |是           	|是			|			|
/roll/entUser									|Get      |企业超管             		|是           	|是			|			|
/securityes/loginCheck							|Get      |登录校验             		|           	|			|			|
/securityes/securitiesLogin						|Post     |证券登录             		|           	|			|			|
/securityes/qryInvitationAward					|Get      |邀请奖励列表查询             |           	|			|			|
/securityes/qryGoldenBean						|Get      |查询金豆个数             	|           	|			|			|
/securityes/qryDataSynTime						|Get      |查询更新时间             	|           	|			|			|
/securityes/qryOpenRewardList					|Get      |查询开户奖励列表             |           	|			|			|
/securityes/qryInvestmentRewardList				|Get      |投资奖励列表             	|           	|			|			|
/securityes/shareInfo							|Get      |查询分享携带信息             |           	|			|			|
/securityes/imageCaptcha						|Get      |生成验证返回图片base64       |           	|			|			|
/securityes/imageValidate						|Get      |验证验证码是否正确           |           	|			|			|
路径											|请求方式 |接口描述           			|参与验签   	|是否测试	|是否白名单	|备注 
/tfinance/list									|Get      |活动产品列表      			|           	|			|			|
/tfinance/product								|Get      |同事团理财产品             	|           	|			|			|
/tfinance/intentionList							|Get      |平台产品预约列表            	|           	|			|			|
/tfinance/operateList							|Get      |操作列表             		|           	|			|			|
/tfinance/intentInfo							|Get      |预约明细             		|           	|			|			|
/tfinance/userInfo								|Get      |预约人信息             		|           	|			|			|
/tfinance/intent								|Post     |预约产品             		|           	|			|			|
/tfinance/codeUrl								|Get      |获取Code的url             	|           	|			|			|
/virus/											|Get      |查询列表             		|           	|			|			|
/virus/post										|Post     |-             				|           	|			|			|
/virus/userInfo									|Get      |-             				|           	|			|			|
/weixin/creatMenu								|Get      |微信菜单创建             	|           	|			|			|
/weixin/deleteMenu								|Get      |微信菜单删除             	|           	|			|			|
/weixin/getMenu									|Get      |微信菜单获取             	|           	|			|			|
/weixin/signature								|Get      |验证消息来自微信             |           	|			|			|
/weixin/signature								|Post     |验证来自微信验签+业务处理 	|           	|是			|是			|
/weixin/wxCallback								|Get      |微信回调接口             	|是         	|是			|			|
/weixin/getJsapiSignature						|Get      |JS分享产生分享签名           |           	|			|			|
/weixin/wagePush								|Get      |工资条推送             		|           	|			|			|todo 确认是否在用
/wisales/welfareActivity/listByPayRoll			|Get      |历史活动记录(员工福利)      	|是           	|是			|			|
/wisales/h5/unAuth/img/{id}						|Get      |图片预览             		|           	|			|			|
/wisales/welfareActivity/detailByPayRoll		|Get      |福利领取活动详情             |是           	|是			|			|
/wisales/welfareGoods/list						|Get      |福利活动商品列表             |           	|			|			|
/wisales/welfareGoods/detail					|Get      |商品详情             		|           	|			|			|
/wisales/welfareCustOrder/welfareExchangeGoods	|Post     |兑换(实物兑换)             	|           	|			|			|
/wisales/welfareCustOrder/welfareExchange		|Post     |兑换(虚拟卡券票星巴克)		|           	|			|			|
/wisales/welfareCustOrder/welfareExchangePhone	|Post     |兑换(话费流量兑换)           |           	|			|			|
/wisales/welfareCust/address/get				|Get      |查询客户收货地址-列表        |是           	|是			|			|
/wisales/welfareCust/address/getById			|Get      |查询 客户地址 信息-地址详情  |是           	|是			|			|
/wisales/welfareCust/address/save				|Post     |新增、修改收货地址           |是           	|是			|			|
/wisales/welfareCust/address/delete				|Post     |删除收货地址             	|是           	|是			|			|
/wisales/welfareCust/area/baseQuery				|Get      |省市区收货地址区域查询       |是           	|是			|			|
/wisales/welfareCust/area/townQuery				|Get      |悠彩乡镇街道收货地址区域查询 |是           	|是			|			|
/wisales/welfareCust/custOrderList				|Get      |查询客户活动兑换记录列表     |           	|			|			|
/wisales/welfareCust/custOrderDetail			|Get      |客户活动兑换明细查询 		|           	|			|			|
/wisales/welfareCust/orderTrack					|Get      |根据下单订单号查询物流信息   |           	|			|			|

















