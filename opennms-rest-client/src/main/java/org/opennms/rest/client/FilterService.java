package org.opennms.rest.client;

public interface FilterService {

	public String setLimit(Integer limit);
	
	public String setOffset(Integer offset);
	
	public String setOrderBy(String field);

	public String setOrderDesc(String queryString);

	public String setLimit(String queryString, Integer limit);
	
	public String setOffset(String queryString, Integer offset);
	
	public String setOrderBy(String queryString, String field);
	
	public String setEqualComparator(String queryString);

	public String setNotEqualComparator(String queryString);
	
	public String setIlikeComparator(String queryString);
	
	public String setLikeComparator(String queryString);
	
	public String setGreaterThanComparator(String queryString);
	
	public String setGreaterEqualsComparator(String queryString);
	
	public String setLessThanComparator(String queryString);
	
	public String setLessEqualsComparator(String queryString);
	
	public String setQuery(String queryString, String sqlStatement);
	
}
