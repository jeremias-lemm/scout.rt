scout.filters = {

  /**
   * Ensures the given parameter is an array
   */
  ensure: function(array) {
    if (!array) {
      return [];
    }
    if (!Array.isArray(array)) {
      return [array];
    }
    return array;
  },

  /**
   * Returns a function that always evaluates to 'true'.
   */
  returnTrue: function() {
    return true;
  },

  /**
   * Returns a function that always evaluates to 'false'.
   */
  returnFalse : function() {
    return false;
  },

  /**
   * Returns a filter to accept only elements which are located outside the given container, meaning not the container itself nor one of its children.
   *
   * @param DOM or jQuery container.
   */
  outsideFilter : function (container) {
    container = container instanceof jQuery ? container[0] : container;
    return function() {
      return this !== container && !$.contains(container, this);
    };
  },

  /**
   * Returns a filter to accept only elements which are not the given element.
   *
   * @param DOM or jQuery element.
   */
  notSameFilter : function(element) {
    element = element instanceof jQuery ? element[0] : element;
    return function() {
      return this !== element;
    };
  }
};
