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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.elasticsearch.Version;
import org.junit.Before;
import org.junit.Test;

public class IndexSettingsTest {

  private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();
  private IndexSettings indexSettings;

  @Before
  public void setUp() throws Exception {
    indexSettings = new IndexSettings(ImmutableMap.of(
        "graylog_0", OBJECTMAPPER.readTree("{\n"
            + "  \"settings\": {\n"
            + "    \"index\": {\n"
            + "      \"version\": {\n"
            + "        \"created\": \"2040499\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}"),
        "graylog_1", OBJECTMAPPER.readTree("{\n"
            + "  \"settings\": {\n"
            + "    \"index\": {\n"
            + "      \"version\": {\n"
            + "        \"created\": \"5061099\"\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}")
    ));
  }

  @Test
  public void indicesCreatedPriorToVersion() {
    final ImmutableMap<String, JsonNode> oldIndices = indexSettings.indicesCreatedPriorToVersion(Version.fromId(5000000));
    assertThat(oldIndices).containsKey("graylog_0");
  }
}