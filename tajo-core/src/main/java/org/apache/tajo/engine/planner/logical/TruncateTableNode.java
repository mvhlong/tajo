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

package org.apache.tajo.engine.planner.logical;

import com.google.gson.annotations.Expose;
import org.apache.tajo.engine.planner.PlanString;
import org.apache.tajo.util.TUtil;

import java.util.List;

public class TruncateTableNode extends LogicalNode {
  @Expose
  private List<String> tableNames;

  public TruncateTableNode(int pid) {
    super(pid, NodeType.TRUNCATE_TABLE);
  }

  public List<String> getTableNames() {
    return tableNames;
  }

  public void setTableNames(List<String> tableNames) {
    this.tableNames = tableNames;
  }

  @Override
  public String toString() {
    return "TruncateTable (table=" + TUtil.collectionToString(tableNames) + ")";
  }

  @Override
  public void preOrder(LogicalNodeVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void postOrder(LogicalNodeVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public PlanString getPlanString() {
    return new PlanString(this);
  }
}
