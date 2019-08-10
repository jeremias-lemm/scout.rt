/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe('Range', function() {

  it('equals', function() {
    expect(new scout.Range(10, 20).equals(new scout.Range(10, 20))).toBe(true);
    expect(new scout.Range(20, 10).equals(new scout.Range(10, 20))).toBe(false);
    expect(new scout.Range(10, 20).equals(new scout.Range(20, 10))).toBe(false);
  });

  describe('add', function() {
    it('returns a new range with the sum of both ranges', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(5, 20);
      expect(range1.add(range2)).toEqual(new scout.Range(0, 20));

      range1 = new scout.Range(5, 20);
      range2 = new scout.Range(0, 10);
      expect(range1.add(range2)).toEqual(new scout.Range(0, 20));

      range1 = new scout.Range(0, 10);
      range2 = new scout.Range(10, 20);
      expect(range1.add(range2)).toEqual(new scout.Range(0, 20));
    });

    it('fails if the new range does not border on the existing', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(11, 20);
      expect(function() {
        range1.add(range2);
      }).toThrow(new Error("Range to add has to border on the existing range. scout.Range[from=0 to=10], scout.Range[from=11 to=20]"));
    });

    it('returns a copy of the non empty range if one range is empty', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(0, 0);
      expect(range1.add(range2)).toEqual(new scout.Range(0, 10));

      range1 = new scout.Range(0, 0);
      range2 = new scout.Range(0, 10);
      expect(range1.add(range2)).toEqual(new scout.Range(0, 10));
    });
  });

  describe('union', function() {
    it('returns a new range with the sum of both ranges', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(5, 20);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 20)]);

      range1 = new scout.Range(5, 20);
      range2 = new scout.Range(0, 10);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 20)]);

      range1 = new scout.Range(0, 10);
      range2 = new scout.Range(10, 20);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 20)]);
    });

    it('returns a copy of both ranges if the ranges don\'t overlap', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(11, 20);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 10), new scout.Range(11, 20)]);
    });

    it('returns a copy of the non empty range if one range is empty', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(0, 0);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 10)]);

      range1 = new scout.Range(0, 0);
      range2 = new scout.Range(0, 10);
      expect(range1.union(range2)).toEqual([new scout.Range(0, 10)]);
    });
  });

  describe('subtract', function() {
    it('returns a new range where the second range is removed from the first', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(5, 20);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 5)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(0, 10);
      expect(range1.subtract(range2)).toEqual([new scout.Range(10, 15)]);
    });

    it('returns a copy of the first range if the second does not overlap the first', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(11, 20);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 10)]);

      range1 = new scout.Range(0, 10);
      range2 = new scout.Range(10, 20);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 10)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(0, 5);
      expect(range1.subtract(range2)).toEqual([new scout.Range(5, 15)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(0, 4);
      expect(range1.subtract(range2)).toEqual([new scout.Range(5, 15)]);
    });

    it('returns an empty range if second range completely covers the first', function() {
      var range1 = new scout.Range(5, 15);
      var range2 = new scout.Range(0, 20);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 0)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(0, 15);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 0)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(5, 20);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 0)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(5, 15);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 0)]);
    });

    it('returns a new range if second range is inside the first and touches a border', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(0, 2);
      expect(range1.subtract(range2)).toEqual([new scout.Range(2, 10)]);

      range1 = new scout.Range(0, 10);
      range2 = new scout.Range(8, 10);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 8)]);
    });

    it('returns an array of two ranges if second range is inside the first but does not touch a border', function() {
      var range1 = new scout.Range(0, 20);
      var range2 = new scout.Range(5, 15);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 5), new scout.Range(15, 20)]);
    });

    it('returns a copy of the first range if the second range is empty', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(0, 0);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 10)]);
    });

    it('returns an empty range if the first range is empty', function() {
      var range1 = new scout.Range(0, 0);
      var range2 = new scout.Range(0, 10);
      expect(range1.subtract(range2)).toEqual([new scout.Range(0, 0)]);
    });

  });

  describe('subtractAll', function() {
    it('subtracts all given ranges', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(5, 20);
      var range3 = new scout.Range(0, 2);
      expect(range1.subtractAll([range2, range3])).toEqual([new scout.Range(2, 5)]);

      range1 = new scout.Range(5, 15);
      range2 = new scout.Range(0, 10);
      range3 = new scout.Range(10, 14);
      expect(range1.subtractAll([range2, range3])).toEqual([new scout.Range(14, 15)]);
    });

    it('may return multiple ranges', function() {
      var range1 = new scout.Range(0, 20);
      var range2 = new scout.Range(5, 10);
      var range3 = new scout.Range(12, 15);
      expect(range1.subtractAll([range2, range3])).toEqual([new scout.Range(0, 5), new scout.Range(10, 12), new scout.Range(15, 20)]);
    });
  });

  describe('intersect', function() {
    it('returns a new range with the part where both ranges overlap', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(5, 20);
      expect(range1.intersect(range2)).toEqual(new scout.Range(5, 10));
    });

    it('returns an empty range if the ranges don\'t overlap', function() {
      var range1 = new scout.Range(0, 10);
      var range2 = new scout.Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new scout.Range(0, 0));

      range1 = new scout.Range(10, 20);
      range2 = new scout.Range(0, 10);
      expect(range1.intersect(range2)).toEqual(new scout.Range(0, 0));
    });

    it('returns an empty range if one range is empty', function() {
      var range1 = new scout.Range(0, 0);
      var range2 = new scout.Range(0, 20);
      expect(range1.intersect(range2)).toEqual(new scout.Range(0, 0));

      range1 = new scout.Range(10, 10);
      range2 = new scout.Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new scout.Range(0, 0));

      range1 = new scout.Range(11, 11);
      range2 = new scout.Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new scout.Range(11, 11));
      expect(range1.intersect(range2).size()).toEqual(0);
    });
  });
});
