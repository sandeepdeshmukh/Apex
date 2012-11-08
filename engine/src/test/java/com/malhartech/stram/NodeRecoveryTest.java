/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.stram;

import com.malhartech.api.BaseOperator;
import com.malhartech.api.CheckpointListener;
import com.malhartech.api.DAG;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.dag.RecoverableInputOperator;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
public class NodeRecoveryTest
{
  private static final Logger logger = LoggerFactory.getLogger(NodeRecoveryTest.class);
  static HashSet<Long> collection = new HashSet<Long>(20);

  public static class CollectorOperator extends BaseOperator implements CheckpointListener
  {
    private int simulateFailure;
    private transient boolean nexttime;
    public final transient DefaultInputPort<Long> input = new DefaultInputPort<Long>(this)
    {
      @Override
      public void process(Long tuple)
      {
//        logger.debug("adding the tuple {}", Long.toHexString(tuple));
        collection.add(tuple);
      }
    };

    /**
     * @param simulateFailure the simulateFailure to set
     */
    public void setSimulateFailure(boolean simulateFailure)
    {
      if (simulateFailure) {
        this.simulateFailure = 1;
      }
      else {
        this.simulateFailure = 0;
      }
    }

    @Override
    public void checkpointed(long windowId)
    {
      if (simulateFailure > 0 && --simulateFailure == 0) {
        nexttime = true;
      }
      else if (nexttime) {
        throw new RuntimeException("Failure Simulation from " + this);
      }
    }

    @Override
    public void committed(long windowId)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  @Test
  public void testInputOperatorRecovery() throws Exception
  {
    int maxTuples = 30;
    DAG dag = new DAG();
    dag.getConf().setInt(DAG.STRAM_CHECKPOINT_INTERVAL_MILLIS, 5000);
    dag.setMaxContainerCount(1);
    RecoverableInputOperator rip = dag.addOperator("LongGenerator", RecoverableInputOperator.class);
    rip.setMaximumTuples(maxTuples);

    CollectorOperator cm = dag.addOperator("LongCollector", CollectorOperator.class);
    cm.setSimulateFailure(true);
    dag.addStream("connection", rip.output, cm.input);

    StramLocalCluster lc = new StramLocalCluster(dag);
    lc.setHeartbeatMonitoringEnabled(false);
    lc.run();

//    for (Long l: collection) {
//      logger.debug(Long.toHexString(l));
//    }
    Assert.assertEquals("Generated Outputs", maxTuples, collection.size());
  }
}
