package mti.commons.elasticsearch.dao;

import java.util.Collection;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mti.commons.elasticsearch.ESPartitionedModel;
import mti.commons.partitions.PartitionManager;

public class ElasticsearchPartitionedDAO<T extends ESPartitionedModel> extends ElasticsearchBasicDAO<T> {

	protected static final Logger logger = LoggerFactory.getLogger(ElasticsearchPartitionedDAO.class);

	protected final PartitionManager partitionManager;

	public ElasticsearchPartitionedDAO(PartitionManager partitionManager, String documentType, Class<T> modelClass) {
		super(partitionManager.getAlias(), documentType, modelClass);
		this.partitionManager = partitionManager;
	}

	@Override
	public T save(T instance) {
		try {
			return (template.index(partitionManager.getPartition(instance.getPartitionDate()), documentType, instance)
					? instance : null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public T findOne(String uuid) {
		FilterBuilder filter = FilterBuilders.idsFilter().addIds(uuid);
		SearchRequestBuilder query = template.NativeSearchQueryBuilder().setIndices(indexTemplate).setTypes(documentType)
				.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), filter));
		
		return template.queryForOne(query, modelClass);
	}

	@Override
	public boolean delete(T instance) {
		try {
			return template.delete(partitionManager.getPartition(instance.getPartitionDate()), documentType, instance);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return false;
	}

	@Override
	public int delete(Collection<T> instances) {
		// ugly but this is not used in production
		int count = 0;
		
		for (T instance: instances) {
			if (delete(instance)) ++count;
		}
		return count;
	}

}
