/**
 * Copyright (c) 2012-2012 Malhar, Inc.
 * All rights reserved.
 */
package com.malhartech.stram.webapp;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "nodes")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodesInfo {

  protected ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();

  public void add(NodeInfo nodeInfo) {
    nodes.add(nodeInfo);
  }

  public ArrayList<NodeInfo> getNodes() {
    return nodes;
  }

}