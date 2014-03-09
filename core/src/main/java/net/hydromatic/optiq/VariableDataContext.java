package net.hydromatic.optiq;

import java.util.Map;

import net.hydromatic.linq4j.QueryProvider;
import net.hydromatic.optiq.impl.java.JavaTypeFactory;

public class VariableDataContext implements DataContext {

	private DataContext wrap;
	private Map<String, Object> variables;
	public VariableDataContext(Map<String, Object> variables, DataContext wrap) {
		this.variables = variables;
		this.wrap = wrap;
	}
	public SchemaPlus getRootSchema() {
		return wrap.getRootSchema();
	}

	public JavaTypeFactory getTypeFactory() {
		return wrap.getTypeFactory();
	}

	public QueryProvider getQueryProvider() {
		return wrap.getQueryProvider();
	}

	public Object get(String name) {
		Object hit = variables.get(name);
		if(hit == null) {
			return wrap.get(name);
		} else {
			return hit;
		}
	}

}
