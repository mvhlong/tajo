/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tajo.jdbc;

import org.apache.tajo.QueryId;
import org.apache.tajo.catalog.Schema;
import org.apache.tajo.client.TajoClient;
import org.apache.tajo.storage.Tuple;

import java.io.IOException;
import java.sql.SQLException;

public class FetchResultSet extends TajoResultSetBase {
  private TajoClient tajoClient;
  private QueryId queryId;
  private int fetchRowNum;
  private TajoMemoryResultSet currentResultSet;
  private boolean finished = false;

  public FetchResultSet(TajoClient tajoClient, Schema schema, QueryId queryId, int fetchRowNum) {
    this.tajoClient = tajoClient;
    this.queryId = queryId;
    this.fetchRowNum = fetchRowNum;
    this.totalRow = Integer.MAX_VALUE;
    this.schema = schema;
  }

  @Override
  protected Tuple nextTuple() throws IOException {
    if (finished) {
      return null;
    }

    try {
      Tuple tuple = null;
      if (currentResultSet != null) {
        currentResultSet.next();
        tuple = currentResultSet.cur;
      }
      if (currentResultSet == null || tuple == null) {
        if (currentResultSet != null) {
          currentResultSet.close();
          currentResultSet = null;
        }
        currentResultSet = tajoClient.fetchNextQueryResult(queryId, fetchRowNum);
        if (currentResultSet == null) {
          finished = true;
          return null;
        }

        currentResultSet.next();
        tuple = currentResultSet.cur;
      }
      if (tuple == null) {
        if (currentResultSet != null) {
          currentResultSet.close();
          currentResultSet = null;
        }
        finished = true;
      }
      return tuple;
    } catch (Throwable t) {
      throw new IOException(t.getMessage(), t);
    }
  }

  @Override
  public void close() throws SQLException {
    if (currentResultSet != null) {
      currentResultSet.close();
      currentResultSet = null;
    }
    tajoClient.closeNonForwardQuery(queryId);
  }
}
