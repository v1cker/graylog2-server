/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog2.indexer.indices;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.elasticsearch.Version;
import org.graylog2.indexer.IndexSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes it easier to ask certain questions about retrieved index settings and
 * exists to avoid spreading knowledge about the internal structure and source of the data structure.
 */
public class IndexSettings {

  private static final Logger LOG = LoggerFactory.getLogger(IndexSettings.class);
  private static final JsonPointer SETTINGS_INDEX_VERSION_CREATED_PTR =
      JsonPointer.valueOf("/settings/index/version/created");
  private static final JsonPointer SETTINGS_INDEX_CREATION_DATE_PTR =
      JsonPointer.valueOf("/settings/index/creation_date");

  private final Map<String, JsonNode> indicesSettings;

  /**
   * For use with {@link Indices#getIndexSettings(IndexSet)} (IndexSet)}.
   * @param indicesSettings the per-index name settings objects
   */
  public IndexSettings(Map<String, JsonNode> indicesSettings) {
    this.indicesSettings = indicesSettings;
  }

  public IndexSettings(String indexName, JsonNode indexSettings) {
    this(ImmutableMap.of(indexName, indexSettings));
  }


  public ImmutableMap<String, JsonNode> indicesCreatedPriorToVersion(Version elasticsearchVersion) {
    ImmutableMap.Builder<String, JsonNode> builder = ImmutableMap.builder();

    indicesSettings.forEach((indexName, settingsJsonNode) -> {
      final JsonNode createdNode = settingsJsonNode.at(SETTINGS_INDEX_VERSION_CREATED_PTR);
      if (createdNode.isValueNode()) {
        final Version version = Version.fromId(createdNode.asInt());
        if (version.before(elasticsearchVersion)) {
          builder.put(indexName, settingsJsonNode);
        }
      } else {
        LOG.warn("Unable to find index.version.created index setting: {}", settingsJsonNode);
      }
    });
    return builder.build();
  }

  public Optional<DateTime> indexCreationDate(String indexName) {
    final JsonNode jsonNode = indicesSettings.getOrDefault(indexName, MissingNode.getInstance());
    return Optional.of(jsonNode.path(indexName).at(SETTINGS_INDEX_CREATION_DATE_PTR))
        .filter(JsonNode::isValueNode)
        .map(JsonNode::asLong)
        .map(creationDate -> new DateTime(creationDate, DateTimeZone.UTC));
  }

}
