package org.sky.auto.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.sky.auto.anno.RunListenerRegister;
import org.sky.auto.base.MyFile;
import org.sky.auto.result.CaseStatus;
import org.sky.auto.result.Environment;
import org.sky.auto.result.MyDescription;
import org.sky.auto.result.MyFailure;
import org.sky.auto.result.MyResult;
import org.sky.auto.intrumentation.*;

import freemarker.template.Template;
@RunListenerRegister
public class ReportListener extends RunListener{
	private MyResult res;
	private MyFailure fail;
	private MyDescription des;
	private TempletHtml th;
	private Environment env;
	private Template temp;
	private Class<?>[] clas;
	private StandardOutInfo soi;
	
	public ReportListener(){
		th=TempletHtml.getInstance();
		MyFile.createDictory("report");
		MyFile.createFile("report"+File.separator+"report.html");
		for(Class<?> clazz: ClassPool.getClassPool()){
			for(Method m:clazz.getDeclaredMethods()){
				if(m.getAnnotation(Test.class)!=null){
					CaseStatus.addNotRunCase(m);
				}
			}
		}
	}
	
	
	@Override
	public void testStarted(Description description) throws Exception {
		//soi=new StandardOutInfo();
		//soi.start();
		env=new Environment();
		String caseName = description.getMethodName()+".ftl";
		MyFile.createFile("report"+File.separator+"case"+caseName);
		des=new MyDescription(description);
		temp=th.getTemplate(caseName);
		for(Method m:description.getTestClass().getDeclaredMethods()){
			if(m.equals(description.getMethodName())){
				clas=m.getParameterTypes();
			}else{
				clas=null;
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void testFinished(Description description) throws Exception {
		if(clas!=null){
			CaseStatus.addRunCase(des.getTestClass().getDeclaredMethod(description.getMethodName(),clas));
		}else{
			CaseStatus.addRunCase(des.getTestClass().getDeclaredMethod(description.getMethodName()));
		}	
		CaseStatus.addRunCaseDescription(description);
		Map map = new HashMap();
		map.put("des",des);
		map.put("env", env);
		Writer write = new OutputStreamWriter(new FileOutputStream(new File("report"+File.separator+"case"+File.separator+des.getMethodName())+".html"));
		temp.process(map, write);
		write.flush();
		write.close();
		//soi.write();
		//soi.clearStream();
	}
	
	@Override
	public void testFailure(Failure failure) throws Exception {
		CaseStatus.addFailureCase(fail);
		CaseStatus.addRunCase(des.getTestClass().getDeclaredMethod(failure.getDescription().getMethodName()));
		fail = new MyFailure(failure);
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void testRunFinished(Result result) throws Exception {
		CssAndJSReport cj=new CssAndJSReport();
		cj.copyCssAndJsSource();
		res=new MyResult(result);
		Set<MyDescription> dess=CaseStatus.getDescriptionCases(); 
		temp=th.getTemplate("templet"+File.separator+"report.ftl");
		Map resmap = new HashMap();
		resmap.put("res",res);
		resmap.put("dess", dess);
		Writer write = new OutputStreamWriter(new FileOutputStream(new File("report"+File.separator+"report.html")));
		temp.process(resmap, write);
		write.flush();
		write.close();
	}
}
