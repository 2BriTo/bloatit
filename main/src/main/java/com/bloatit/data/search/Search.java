package com.bloatit.data.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.util.Version;
import org.hibernate.search.FullTextFilter;
import org.hibernate.search.FullTextQuery;

import com.bloatit.data.NullCollection;
import com.bloatit.data.SessionManager;
import com.bloatit.framework.utils.PageIterable;

public abstract class Search<T> {


    private String searchStr;
    private Class<T> persistent;
    private String[] fields;
    private String filter = null;
    private final List<Pair<String,String>> filteredTerms = new ArrayList<Pair<String,String>>();
    private Sort sort = null;

    protected void configure(final Class<T> persistent, final String[] fields, final String searchStr) {
        this.persistent = persistent;
        this.fields = fields;
        this.searchStr = searchStr;
    }


    /**
     * Create a search on the db using Hibernate Search and Lucene
     *
     * <pre>
     * DBRequests.search(DaoDemand.class, new String[] { &quot;description.translations.title&quot;, &quot;description.translations.text&quot;,
     *         &quot;offers.description.translations.title&quot; }, &quot;Search string&quot;);
     * </pre>
     *
     * @param <T> is a persistent class (something like Dao...)
     * @param persistent is the class object associated with T.
     * @param fields is a list of field on which we are doing the search. These field are
     *        relative to the persistent class.
     * @param searchStr is the string we are looking for.
     * @return a PageIterable with the search results.
     */
    protected final PageIterable<T> doSearch() {
        prepareSearch();

        final org.apache.lucene.search.Query query;

        if(searchStr.equals("")) {
            query = new MatchAllDocsQuery();
        } else {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_29, fields, new StandardAnalyzer(Version.LUCENE_29));
            try {
                query = parser.parse(searchStr);
            } catch (ParseException e) {
                return new NullCollection<T>();
            }
        }

        FullTextQuery luceneQuery = SessionManager.getCurrentFullTextSession().createFullTextQuery(query, persistent);

        applyFilters(luceneQuery);


        return new SearchCollection<T>(luceneQuery);

    }

    private void applyFilters(FullTextQuery luceneQuery) {
        if(filter != null) {
            FullTextFilter fullTextFilter = luceneQuery.enableFullTextFilter(filter);

            fullTextFilter.setParameter("filteredTerms", filteredTerms);
        }
        if(sort != null) {
            luceneQuery.setSort(sort);
        }

    }


    protected abstract void prepareSearch();


    protected void addFilterTerm(String term, String value) {
        filteredTerms.add(new Pair<String, String>(term, value));
    }


    protected void enableFilter(String filter) {
        this.filter = filter;


    }


    protected void setSort(Sort sort) {
        this.sort = sort;
    }


    public static class Pair<U, V> {

        public final U key;
        public final V value;

        public Pair(U key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }



    }

}