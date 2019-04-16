import Widget from '../widget/Widget';
import HtmlComponent from '../layout/HtmlComponent';
import Scout from '../Scout';

export default class ViewMenuTab extends Widget {

  constructor() {
    super();
    this.viewButtons = [];
    this.selected = false;
    this.viewTabVisible = true;
    //this.defaultIconId = scout.icons.FOLDER;
    this._addWidgetProperties(['selectedButton']);
  }

  _init(model) {
    super._init(model);
    /*this.dropdown = scout.create('Menu', {
        parent: this,
        //iconId: scout.icons.ANGLE_DOWN,
        tabbable: false,
        cssClass: 'view-menu'
    });
    this.dropdown.on('action', this.togglePopup.bind(this));*/
    this._setViewButtons(this.viewButtons);
  };

  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    // Bound to desktop
    /*this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
    this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
    this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
    this.desktopKeyStrokeContext.registerKeyStroke([
        new scout.ViewMenuOpenKeyStroke(this)
    ]);*/
  };

  _render() {
    this.$container = this.$parent.appendDiv('view-tab');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    //this.dropdown.render(this.$container);
    //this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  };

  _remove() {
    //this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
    super._remove();
    if (this.selectedButton) {
      this.selectedButton.remove();
    }
  };

  _renderProperties() {
    super._renderProperties();
    this._updateSelectedButton();
  };

  setViewButtons(viewButtons) {
    this.setProperty('viewButtons', viewButtons);
  };

  _setViewButtons(viewButtons) {
    this._setProperty('viewButtons', viewButtons);
    this.setVisible(this.viewButtons.length > 0);
    var selectedButton = this._findSelectedViewButton();
    if (selectedButton) {
      this.setSelectedButton(selectedButton);
    } else {
      this.setSelectedButton(this.viewButtons[0]);
    }
    this.setSelected(!!selectedButton);
  };

  setSelectedButton(viewButton) {
    if (this.selectedButton && this.selectedButton.cloneOf === viewButton) {
      return;
    }
    if (viewButton) {
      this.setProperty('selectedButton', viewButton);
    }
  };

  _setSelectedButton(viewButton) {
    var outlineParent = null;
    if (viewButton.outline) {
      outlineParent = viewButton.outline.parent;
    }
    viewButton = viewButton.clone({
      parent: this,
      displayStyle: 'TAB'
    }, {
      delegateEventsToOriginal: ['acceptInput', 'action'],
      delegateAllPropertiesToClone: true,
      delegateAllPropertiesToOriginal: true,
      excludePropertiesToOriginal: ['selected']
    });
    if (outlineParent) {
      viewButton.outline.setParent(outlineParent);
    }
    // use default icon if outline does not define one.
    viewButton.iconId = viewButton.iconId || this.defaultIconId;
    this._setProperty('selectedButton', viewButton);
  };

  _renderSelectedButton() {
    this._updateSelectedButton();
  };

  _updateSelectedButton() {
    if (!this.selectedButton) {
      return;
    }
    if (this.viewTabVisible) {
      if (!this.selectedButton.rendered) {
        this.selectedButton.render(this.$container);
        this.invalidateLayoutTree();
      }
    } else {
      if (this.selectedButton.rendered) {
        this.selectedButton.remove();
        this.invalidateLayoutTree();
      }
    }
  };

  setViewTabVisible(viewTabVisible) {
    this.setProperty('viewTabVisible', viewTabVisible);
    if (this.rendered) {
      this._updateSelectedButton();
    }
  };

  _renderSelected() {
    this.$container.select(this.selected);
  };

  _findSelectedViewButton() {
    var viewMenu;
    for (var i = 0; i < this.viewButtons.length; i++) {
      viewMenu = this.viewButtons[i];
      if (viewMenu.selected) {
        return viewMenu;
      }
    }
    return null;
  };

  /**
   * Toggles the 'view menu popup', or brings the outline content to the front if in background.
   */
  togglePopup() {
    if (this.popup) {
      this._closePopup();
    } else {
      this._openPopup();
    }
  };

  _openPopup() {
    if (this.popup) {
      // already open
      return;
    }
    var naviBounds = Graphics.bounds(this.$container.parent(), true);
    this.popup = Scout.create('ViewMenuPopup', {
      parent: this,
      $tab: this.dropdown.$container,
      viewMenus: this.viewButtons,
      naviBounds: naviBounds
    });
    // The class needs to be added to the container before the popup gets opened so that the modified style may be copied to the head.
    this.$container.addClass('popup-open');
    this.popup.headText = this.text;
    this.popup.open();
    this.popup.on('remove', function(event) {
      this.$container.removeClass('popup-open');
      this.popup = null;
    }.bind(this));
  };

  _closePopup() {
    if (this.popup) {
      this.popup.close();
    }
  };

  setSelected(selected) {
    this.setProperty('selected', selected);
  };

  sendToBack() {
    this._closePopup();
  };

  bringToFront() {
    // NOP
  };

  onViewButtonSelected() {
    var viewButton = this._findSelectedViewButton();
    if (viewButton) {
      this.setSelectedButton(this._findSelectedViewButton());
    }
    this.setSelected(!!viewButton);
    this._closePopup();
  };

}