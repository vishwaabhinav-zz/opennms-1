package org.opennms.rest.client.internal;

public class JerseyAbstractService {

	public String setLimit(Integer limit) {
		return "limit="+Integer.toString(limit);
	}
	
	public String setOffset(Integer offset){
		return "offset="+Integer.toString(offset);
	}
	
	public String setOrderBy(String field) {
		return "orderBy="+field;
	}
	
	public String setOrderDesc(String queryString) {
		return queryString+"&"+"order=desc";
	}

	public String setLimit(String queryString, Integer limit) {
		return queryString+"&"+setLimit(limit);
	}
	
	public String setOffset(String queryString, Integer offset) {
		return queryString+"&"+setOffset(offset);
	}
	
	public String setOrderBy(String queryString, String field) {
		return queryString+"&"+setOrderBy(field);
	}
	
	public String setEqualComparator(String queryString) {
		return queryString+"&comparator=eq";
	}

	public String setNotEqualComparator(String queryString) {
		return queryString+"&comparator=ne";
	}
	
	public String setIlikeComparator(String queryString) {
		return queryString+"&comparator=ilike";
	}
	
	public String setLikeComparator(String queryString) {
		return queryString+"&comparator=like";
	}
	
	public String setGreaterThanComparator(String queryString) {
		return queryString+"&comparator=gt";
	}
	
	public String setGreaterEqualsComparator(String queryString) {
		return queryString+"&comparator=ge";
	}
	
	public String setLessThanComparator(String queryString) {
		return queryString+"&comparator=lt";
	}
	
	public String setLessEqualsComparator(String queryString) {
		return queryString+"&comparator=le";
	}
	
	public String setQuery(String queryString, String sqlStatement) {
		return queryString+"&query="+sqlStatement;
	}
	
}
