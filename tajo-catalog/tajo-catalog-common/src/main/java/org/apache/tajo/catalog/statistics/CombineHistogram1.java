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

package org.apache.tajo.catalog.statistics;

import java.util.List;

import org.apache.tajo.catalog.proto.CatalogProtos.HistogramProto;
import org.apache.tajo.util.TUtil;

public class CombineHistogram1 extends Histogram {

  public CombineHistogram1() {
    super();
  }

  public CombineHistogram1(HistogramProto proto) {
    super(proto);
  }

  @Override
  public boolean construct(List<Double> samples) {
    int numBuckets = samples.size() > DEFAULT_MAX_BUCKETS ? DEFAULT_MAX_BUCKETS : samples.size();
    return construct(samples, numBuckets);
  }
  
  public boolean construct(List<Double> samples, int numBuckets) {
    isReady = false;
    buckets = TUtil.newList();
    EquiWidthHistogram h1 = new EquiWidthHistogram();
    EquiDepthHistogram h2 = new EquiDepthHistogram();
    
    int h1NumBuckets = numBuckets / 2;
    h1.construct(samples, h1NumBuckets);
    h2.construct(samples, numBuckets - h1.getBucketsCount());
    
    setBuckets(h1.getBuckets());
    buckets.addAll(h2.getBuckets());
    
    isReady = true;
    lastAnalyzed = System.currentTimeMillis();

    return true;
  }
  
  @Override
  public double estimateSelectivity(Double from, Double to) {
    if(from > to) return 0;
    if(!isReady) return -1;
    Double freq = estimateFrequency(from, to);
    Double totalFreq = 0.0;
    for(HistogramBucket bucket : buckets) {
      totalFreq += bucket.getFrequency();
    }
    totalFreq = totalFreq / 2; // because "this.buckets" contains multiple histograms 
    double selectivity = freq / totalFreq;
    return selectivity;
  }
  
  @Override
  public Double estimateFrequency(Double from, Double to) {
    // Reconstruct bucket lists of individual histograms
    List<HistogramBucket> buckets1, buckets2;
    buckets1 = TUtil.newList();
    buckets2 = TUtil.newList();
    int idx1 = buckets.size() / 2;
    int idx2 = buckets.size() - 1;
    buckets1.addAll(buckets.subList(0, idx1));
    buckets2.addAll(buckets.subList(idx1 + 1, idx2));

    // Compute the confidence coefficients (cc (s))
    List<Double> overlapRatios1 = TUtil.newList();
    List<Double> overlapRatios2 = TUtil.newList();

    for (HistogramBucket bucket : buckets1) {
      Double r = computeOverlapRatio(from, to, bucket);
      overlapRatios1.add(r);
    }
    for (HistogramBucket bucket : buckets2) {
      Double r = computeOverlapRatio(from, to, bucket);
      overlapRatios2.add(r);
    }

    double cc1 = computeListAverage(overlapRatios1);
    double cc2 = computeListAverage(overlapRatios2);

    // Estimate frequency
    Double estimate = 0.0;

    if (cc1 > cc2) {
      for (HistogramBucket bucket : buckets1) {
	estimate += estimateFrequency(bucket, from, to);
      }
    } else {
      for (HistogramBucket bucket : buckets2) {
	estimate += estimateFrequency(bucket, from, to);
      }
    }
    return estimate;
  }
}
