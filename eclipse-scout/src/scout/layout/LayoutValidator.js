import Arrays from '../utils/Arrays';

export default class LayoutValidator {

    constructor() {
        this._invalidComponents = [];
        this._validateTimeoutId = null;
        this._postValidateFunctions = [];
    }

   invalidateTree(htmlComp) {
        var validateRoot,
            htmlParent = htmlComp,
            htmlSource = htmlComp;

        // Mark every parent as invalid until validate root
        while (htmlParent) {
            htmlComp = htmlParent;
            htmlComp.invalidateLayout(htmlSource);
            if (htmlComp.isValidateRoot()) {
                validateRoot = htmlComp;
                break;
            }
            htmlParent = htmlComp.getParent();
        }

        if (!htmlParent) {
            validateRoot = htmlComp;
        }

        this.invalidate(validateRoot);
    };

    invalidate(htmlComp) {
        var position = 0;
        // Don't insert if already inserted...
        // Info: when component is already in list but no one triggers validation,
        // validation is never scheduled that's why we call scheduleValidation here.
        if (this._invalidComponents.indexOf(htmlComp) >= 0) {
            this._scheduleValidation(); // ... but schedule validation
            return;
        }

        // Make sure it will be inserted before any descendant
        // This prevents multiple layouting of the descendant
        this._invalidComponents.forEach(function(invalidComponent, i) {
            if (invalidComponent.isDescendantOf(htmlComp)) {
                return;
            }
            position++;
        }, this);

        // Add validate root to list of invalid components. These are the starting point for a subsequent call to validate().
        Arrays.insert(this._invalidComponents, htmlComp, position);

        this._scheduleValidation();
    };

    _scheduleValidation() {
        if (this._validateTimeoutId === null) {
            this._validateTimeoutId = setTimeout(function() {
                this.validate();
            }.bind(this));
        }
    };

    /**
     * Layouts all invalid components (as long as they haven't been removed).
     */
    validate() {
        clearTimeout(this._validateTimeoutId);
        this._validateTimeoutId = null;
        this._invalidComponents.slice().forEach(function(comp) {
            if (comp.validateLayout()) {
                Arrays.remove(this._invalidComponents, comp);
            }
        }, this);
        this._postValidateFunctions.slice().forEach(function(func) {
            func();
            Arrays.remove(this._postValidateFunctions, func);
        }, this);
    };

    /**
     * Removes those components from this._invalidComponents which have the given container as ancestor.
     * The idea is to remove all components whose ancestor is about to be removed from the DOM.
     */
    cleanupInvalidComponents($parentContainer){
        this._invalidComponents.slice().forEach(function(comp){
            if (comp.$comp.closest($parentContainer).length > 0){
                Arrays.remove(this._invalidComponents, comp);
            }
        }, this);
    };

    /**
     * Runs the given function at the end of validate().
     */
    schedulePostValidateFunction(func) {
        if (func) {
            this._postValidateFunctions.push(func);
        }
    };


}