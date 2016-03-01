/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
var OutlineSpecHelper = function(session) {
  this.session = session;
};

OutlineSpecHelper.prototype.createModelFixture = function(nodeCount, depth, expanded) {
  return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
};

OutlineSpecHelper.prototype.createModel = function(nodes) {
  var model = createSimpleModel('Outline', this.session);

  if (nodes) {
    model.nodes = nodes;
  }

  return model;
};

OutlineSpecHelper.prototype.createModelNode = function (id, text) {
  return {
    "id": id,
    "text": text
  };
};

OutlineSpecHelper.prototype.createModelNodes = function (nodeCount, depth, expanded) {
  return this.createModelNodesInternal(nodeCount, depth, expanded);
};

OutlineSpecHelper.prototype.createModelNodesInternal = function(nodeCount, depth, expanded, parentNode) {
  if (!nodeCount) {
    return;
  }

  var nodes = [],
    nodeId;
  if (!depth) {
    depth = 0;
  }
  for (var i = 0; i < nodeCount; i++) {
    nodeId = i;
    if (parentNode) {
      nodeId = parentNode.id + '_' + nodeId;
    }
    nodes[i] = this.createModelNode(nodeId, 'node ' + i);
    nodes[i].expanded = expanded;
    if (depth > 0) {
      nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
    }
  }
  return nodes;
};

OutlineSpecHelper.prototype.createOutline = function(model) {
  var tree = new scout.Outline();
  tree.init(model);
  return tree;
};
