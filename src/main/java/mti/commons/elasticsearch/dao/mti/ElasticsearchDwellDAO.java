package mti.commons.elasticsearch.dao.mti;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.vividsolutions.jts.geom.Geometry;

import mti.commons.elasticsearch.dao.ElasticsearchPartitionedDAO;
import mti.commons.model.Dwell;
import mti.commons.partitions.PartitionManager;
import mti.commons.repositories.filters.GeoFilter;
import mti.commons.repositories.filters.TimeFilter;

public class ElasticsearchDwellDAO extends
	ElasticsearchPartitionedDAO<Dwell>
{

	public static String DEFAULT_SORT_BY = "dateTime";

	public ElasticsearchDwellDAO(
		PartitionManager partitionManager ) {
		super(
			partitionManager,
			"gmti_dwell",
			Dwell.class);
	}

	public Dwell findOneByDwellUID(
		Long dwellUID ) {
		FilterBuilder preFilter = FilterBuilders.termFilter(
			"dwellUID",
			dwellUID);

		SearchRequestBuilder searchQuery = template
			.NativeSearchQueryBuilder()
			.setIndices(
				partitionManager.getAlias())
			.setTypes(
				documentType)
			.setQuery(
				QueryBuilders.filteredQuery(
					QueryBuilders.matchAllQuery(),
					preFilter));

		Dwell d = template.queryForOne(
			searchQuery,
			Dwell.class);

		return d;
	}

	public List<Dwell> pageByMissionUIDAndJobDefinitionUID(
		Long missionUID,
		Long jobDefinitionUID,
		int pageNumber,
		int pageSize,
		String sortBy) {
		AndFilterBuilder preFilter = FilterBuilders.andFilter(
			FilterBuilders.termFilter(
				"missionUID",
				missionUID),
			FilterBuilders.termFilter(
				"jobDefinitionUID",
				jobDefinitionUID));

		SearchRequestBuilder searchQuery = template
			.NativeSearchQueryBuilder()
			.setIndices(
				partitionManager.getAlias())
			.setTypes(
				documentType)
			.addSort(
				sortBy,
				SortOrder.ASC)
			.setQuery(
				QueryBuilders.filteredQuery(
					QueryBuilders.matchAllQuery(),
					preFilter));

		List<Dwell> dwells = template.queryForPage(
			searchQuery,
			pageNumber,
			pageSize,
			Dwell.class);

		return dwells;
	}

	public List<Dwell> getDwellsByList(
		Long missionUID,
		Long jobDefinitionUID,
		Date start,
		Date end,
		List<Geometry> geometries ) {
		String[] partitions = partitionManager.getPartitions(
			start,
			end);
		if (partitions.length == 0) return Collections.emptyList();

		AndFilterBuilder preFilter = FilterBuilders.andFilter(
			FilterBuilders.termFilter(
				"missionUID",
				missionUID),
			FilterBuilders.termFilter(
				"jobDefinitionUID",
				jobDefinitionUID),
			TimeFilter.getTimeRangeFilter(
				"dateTime",
				start,
				end));

		SearchRequestBuilder searchQuery = template
			.NativeSearchQueryBuilder()
			.setIndices(
				partitions)
			.setTypes(
				documentType)
			.addSort(
				DEFAULT_SORT_BY,
				SortOrder.ASC)
			.setQuery(
				QueryBuilders.filteredQuery(
					QueryBuilders.matchAllQuery(),
					preFilter));

		if (geometries != null && !geometries.isEmpty()) searchQuery.setPostFilter(
			GeoFilter.getGeometryFilter(
				"boundingArea",
				geometries));

		List<Dwell> dwells = template.queryForList(
			searchQuery,
			Dwell.class);

		return dwells;
	}
}
