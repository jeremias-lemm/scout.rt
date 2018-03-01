/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TileAccordion", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createAccordion(numGroups, model) {
    var groups = [];
    for (var i = 0; i < numGroups; i++) {
      groups.push({
        objectType: 'Group',
        label: "Group " + i,
        body: {
          objectType: 'TileGrid',
          scrollable: false
        }
      });
    }
    var defaults = {
      parent: session.desktop,
      groups: groups
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileAccordion', model);
  }

  function createGroup(model) {
    var defaults = {
      parent: session.desktop,
      body: {
        objectType: 'TileGrid',
        scrollable: false
      }
    };
    model = $.extend({}, defaults, model);
    return scout.create('Group', model);
  }

  function createTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('Tile', model);
  }

  describe('init', function() {
    it('copies properties to tile grids', function() {
      var comparator = function() {
        return true;
      };
      var accordion = createAccordion(0, {
        selectable: true,
        multiSelect: false,
        tileGridLayoutConfig: {
          columnWidth: 100,
          rowHeight: 100
        },
        tileComparator: comparator,
        gridColumnCount: 2,
        withPlaceholders: true
      });
      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid'
        }
      });
      expect(accordion.groups[0].body.selectable).toBe(true);
      expect(accordion.groups[0].body.multiSelect).toBe(false);
      expect(accordion.groups[0].body.layoutConfig).toEqual(scout.TileGridLayoutConfig.ensure({
        columnWidth: 100,
        rowHeight: 100
      }));
      expect(accordion.groups[0].body.comparator).toBe(comparator);
      expect(accordion.groups[0].body.gridColumnCount).toBe(2);
      expect(accordion.groups[0].body.withPlaceholders).toBe(true);
    });

    it('does not override properties which are specified by the tile grid itself', function() {
      var comparator = function() {
        return true;
      };
      var accordion = createAccordion(0);
      accordion.insertGroup({
        objectType: 'Group',
        body: {
          objectType: 'TileGrid',
          selectable: true,
          multiSelect: false,
          layoutConfig: {
            columnWidth: 100,
            rowHeight: 100
          },
          comparator: comparator,
          gridColumnCount: 2,
          withPlaceholders: true
        }
      });
      expect(accordion.groups[0].body.selectable).toBe(accordion.selectable);
      expect(accordion.groups[0].body.multiSelect).toBe(accordion.multiSelect);
      expect(accordion.groups[0].body.layoutConfig).toEqual(accordion.tileGridLayoutConfig);
      expect(accordion.groups[0].body.comparator).toEqual(accordion.tileComparator);
      expect(accordion.groups[0].body.gridColumnCount).toBe(accordion.gridColumnCount);
      expect(accordion.groups[0].body.withPlaceholders).toBe(accordion.withPlaceholders);
    });
  });

  describe('setters', function() {
    it('copy properties to tile grids', function() {
      var accordion = createAccordion(2);

      expect(accordion.selectable).toBe(false);
      accordion.setSelectable(true);
      expect(accordion.selectable).toBe(true);
      expect(accordion.groups[0].body.selectable).toBe(true);
      expect(accordion.groups[1].body.selectable).toBe(true);

      expect(accordion.multiSelect).toBe(true);
      accordion.setMultiSelect(false);
      expect(accordion.multiSelect).toBe(false);
      expect(accordion.groups[0].body.multiSelect).toBe(false);
      expect(accordion.groups[1].body.multiSelect).toBe(false);
    });
  });

  describe('click', function() {
    it('triggers tileClick', function() {
      var accordion = createAccordion(3, {
        selectable: false,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      var clickEventCount = 0;
      accordion.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(false);
      expect(clickEventCount).toBe(1);
    });

    it('triggers tileSelected and tileClick if selectable', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      var clickEventCount = 0;
      var selectEventCount = 0;
      var events = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      accordion.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
        events.push('click');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);

      tile0.$container.triggerClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(events.length).toBe(2);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
    });

    it('triggers tileAction when clicked twice', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      var selectEventCount = 0;
      var clickEventCount = 0;
      var actionEventCount = 0;
      var events = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          selectEventCount++;
        }
        events.push('select');
      });
      accordion.on('tileClick', function(event) {
        if (event.tile === tile0) {
          clickEventCount++;
        }
        events.push('click');
      });
      accordion.on('tileAction', function(event) {
        if (event.tile === tile0) {
          actionEventCount++;
        }
        events.push('action');
      });
      expect(tile0.selected).toBe(false);
      expect(selectEventCount).toBe(0);
      expect(clickEventCount).toBe(0);
      expect(actionEventCount).toBe(0);

      tile0.$container.triggerDoubleClick();
      expect(tile0.selected).toBe(true);
      expect(selectEventCount).toBe(1);
      expect(clickEventCount).toBe(1);
      expect(actionEventCount).toBe(1);
      expect(events.length).toBe(3);
      expect(events[0]).toBe('select');
      expect(events[1]).toBe('click');
      expect(events[2]).toBe('action');
    });

    it('is not delegated anymore if group is deleted without being destroyed', function() {
      // This is a theoretical proof of concept without any known practical use cases
      var accordion = createAccordion(3, {
        selectable: false,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();
      var clickEventCount = 0;
      accordion.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount++;
        }
      });
      expect(clickEventCount).toBe(0);

      // Use desktop as owner to prevent destruction
      var group0 = accordion.groups[0];
      group0.setOwner(session.desktop);
      accordion.deleteGroup(group0);
      expect(group0.destroyed).toBe(false);
      expect(group0.rendered).toBe(false);

      // Move to another accordion
      var accordion2 = createAccordion(0);
      accordion2.insertGroup(group0);
      accordion2.render();
      var clickEventCount2 = 0;
      accordion2.on('tileClick', function(event) {
        if (event.tile === tile0 && event.mouseButton === 1) {
          clickEventCount2++;
        }
      });
      expect(group0.rendered).toBe(true);

      // First accordion must not delegate anymore but second has to
      tile0.$container.triggerClick();
      expect(clickEventCount).toBe(0);
      expect(clickEventCount2).toBe(1);
    });
  });

  describe('selectTiles', function() {
    it('selects one of the given tiles and unselects the previously selected ones', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);

      accordion.selectTiles(tile0);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile0);

      accordion.selectTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);

      accordion.selectTiles([tile0, tile1]);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);
    });

    it('selects all the given tiles and unselects the previously selected ones if multiSelect is true', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);

      accordion.selectTiles(tile0);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile0);

      accordion.selectTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(1);
      expect(accordion.getSelectedTile()).toBe(tile1);

      accordion.selectTiles([tile0, tile1]);
      expect(accordion.getSelectedTileCount()).toBe(2);
      expect(accordion.getSelectedTiles()[0]).toBe(tile0);
      expect(accordion.getSelectedTiles()[1]).toBe(tile1);
    });

    it('triggers a property change event', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: false
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      var eventTriggered = false;
      var selectedTiles = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = event.newValue;
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(1);
      expect(selectedTiles[0]).toBe(tile1);
    });

    it('triggers a property change event also if multiSelect is true', function() {
      var accordion = createAccordion(3, {
        selectable: true,
        multiSelect: true
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[1].body.insertTile(tile1);
      expect(accordion.getSelectedTileCount()).toBe(0);
      var eventTriggered = false;
      var selectedTiles = [];
      accordion.on('propertyChange', function(event) {
        if (event.propertyName === 'selectedTiles') {
          eventTriggered = true;
          selectedTiles = event.newValue;
        }
      });
      accordion.selectTiles([tile0, tile1]);
      expect(eventTriggered).toBe(true);
      expect(selectedTiles.length).toBe(2);
      expect(selectedTiles[0]).toBe(tile0);
      expect(selectedTiles[1]).toBe(tile1);
    });

    it("does not select tiles in a collapsed group", function() {
      var accordion = createAccordion(2, {
        selectable: true,
        gridColumnCount: 3
      });
      var tile0 = createTile();
      var tile1 = createTile();
      accordion.groups[0].body.insertTile(tile0);
      accordion.groups[0].setCollapsed(true);
      accordion.groups[1].body.insertTile(tile1);
      accordion.render();

      accordion.selectTile(tile0);
      expect(accordion.groups[0].body.selectedTiles.length).toBe(0);
      expect(accordion.getSelectedTiles().length).toBe(0);
      expect(tile0.selected).toBe(false);

      accordion.groups[0].setCollapsed(false);
      accordion.selectTile(tile0);
      expect(accordion.groups[0].body.selectedTiles.length).toBe(1);
      expect(accordion.getSelectedTiles().length).toBe(1);
      expect(tile0.selected).toBe(true);
    });
  });
});
