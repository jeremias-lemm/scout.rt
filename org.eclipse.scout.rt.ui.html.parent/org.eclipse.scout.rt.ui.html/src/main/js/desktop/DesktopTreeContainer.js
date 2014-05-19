scout.DesktopTreeContainer = function($parent, model, session) {
  this.session = session;
  this._$desktopTree = $parent.appendDiv('DesktopTree');
  this.$div = this._$desktopTree;
  this.desktopTree = new scout.DesktopTree(this._$desktopTree, model, session);
};

scout.DesktopTreeContainer.prototype.onOutlineCreated = function(event) {
  if (this.desktopTree) {
    this.desktopTree.detach();
  }
  this.desktopTree = new scout.DesktopTree(this._$desktopTree, event, this.session);
  this.desktopTree.attachModel();
};

scout.DesktopTreeContainer.prototype.onOutlineChanged = function(outlineId) {
  this.desktopTree.detach();
  this.desktopTree = this.session.widgetMap[outlineId];
  this.desktopTree.attach(this._$desktopTree);
  this.desktopTree.attachModel();
};

scout.DesktopTreeContainer.prototype.attachModel = function() {
  //TODO attachModel only necessary because setNodeSelection relies on desktop bench which is created later, is there a better way?
  this.desktopTree.attachModel();
};
