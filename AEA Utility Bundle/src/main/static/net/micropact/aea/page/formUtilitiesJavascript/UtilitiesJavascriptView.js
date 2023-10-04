/* AEA - Form - Utilities - Javascript
 *
 * This file contains javascript functions which are intended to be used internally by the System Design team.
 * The API of this file may change and should not be relied upon by project teams.
 *
 * ZRM */
"use strict";

/* Namespaced as AeaFormUtilitiesJavascriptLibrary */
var AeaFormUtilitiesJavascriptLibrary = function(jQ, u) {

    /* RELOAD FUNCTIONS */

    var reloadFunctions
	var core_onCompleteEventHandler

    if (u) {
        reloadFunctions = u.reloadFunctions
    } else {
        reloadFunctions = []
    }

    function addOnreloadEvent(fnName) {
        reloadFunctions[reloadFunctions.length] = fnName
    }
    if (typeof window.onCompleteEventHandler != 'undefined') {
        core_onCompleteEventHandler = onCompleteEventHandler
    }
    window.onCompleteEventHandler = function() {
        core_onCompleteEventHandler.apply(window, arguments)
        reloadFunctions.forEach(function(reloadFunction){
        	reloadFunction()
        })
    }

    function addMultiloadEvent(fnName) {
        jQuery(fnName)
        addOnreloadEvent(fnName)
    }

    //Sets width to auto
    function resizeElementWidth(elem) {
        elem.style.width = 'auto'
    }

    //Sets height to auto
    function resizeElementHeight(elem) {
        elem.style.height = 'auto'
    }

    //makes it to checkboxes don't get cut off like entellitrak does by default
    function autoResizeMultiSelects() {
    	jQuery("div").toArray()
    		.filter(function(div){
    			return div.id.indexOf("_multiValue") > -1
    		}).forEach(function(div){
    			resizeElementWidth(div)
    			resizeElementHeight(div)
    		})
    }

    /* Determine if we are on the create form for a data object (as opposed to update) */
    function isCreate() {
        return getValue("update") === "false"
    }

    /* dynamically loads a css style sheet */
    function loadStyleSheet(url) {
        if (document.createStyleSheet) {
            document.createStyleSheet(url)
        } else {
            var cssNode = document.createElement('link')
            cssNode.rel = 'stylesheet'
            cssNode.href = url
            document.getElementsByTagName("head")[0].appendChild(cssNode)
        }
    }

    /* Finds the first element in the array of objects with searchedProperty = searchedValue
     * Then returns the returnedProperty of that object */
    function lookupValueInArray(array, searchedProperty, searchedValue, returnedProperty) {
        var filtered = filterArray(array, searchedProperty, searchedValue)
        if (filtered.length > 0) {
            return filtered[0][returnedProperty]
        } else {
            return null
        }
    }

    /* Filters an array of objects with searchedProperty = searchedValue */
    function filterArray(array, searchedProperty, searchedValue) {
        return jQ(array).filter(function() {
            return this[searchedProperty] == searchedValue
        })
    }

    /*Gets the value of the input specified by name*/
    function getValue(name) {
        if (jQ('#' + name + '_multiValue').length > 0) {
            if (jQ('[name=' + name + ']:checkbox').length > 0) {
                return jQ('[name=' + name + ']:checked').map(function(_index, box) {
                    return box.value
                })
            } else {
                return jQ('input[name=' + name + ']').map(function(_index, input) {
                    return input.value
                })
            }
        } else {
            return jQ('#' + name + ', input[type=hidden][name=' + name + '], #' + name + '_yes:checked, #' + name + '_no:checked, input[name=' + name + ']:checked').val()
        }
    }

    function nvl(test, option) {
        return test ? test : option
    }

    function nvl2(test, trueOption, falseOption) {
        return test ? trueOption : falseOption
    }

    /* returns a required icon*/
    function generateRequiredIcon() {
        return jQuery('<img title="Required" alt="Required" style="height: 10px; width: 10px;" src="themes/default/web-pub/images/icons/required.gif">')
    }

    function appendTD(tableRow, element) {
        var td = document.createElement('td')
        td.appendChild(element)
        $(tableRow).childElements().last().colSpan = "1"
        tableRow.appendChild(td)
    }

    function createFileLink(fileId, fileName) {
        return jQuery('<a>')
            .attr({
                href: 'tracking.file.do?id=' + fileId
            })
            .append(jQuery('<img>')
                .attr({
                    border: '0',
                    align: 'middle',
                    alt: 'Download',
                    src: "themes/default/web-pub/images/icons/16x16/document_attachment.png"
                }))
            .append(jQuery('<span>')
                .text(fileName))
    }

    function dialogAlert(jQueryElement) {
        var zIndex = maximumZIndex() + 1

        var dialog = jQueryElement.dialog({
            modal: true,
            title: "Alert"
        })

        processZIndex(dialog, zIndex)
    }

    /* This function should be called after a dialog is opened. It will adjust the zIndex of the dialog
     * because if we don't, entellitrak's menus will appear over top of the dialog box. */
    function processZIndex(dialog, zIndex) {
        dialog.parents('.ui-dialog:first')[0].style.setProperty("z-index", zIndex + 1, "important")

        dialog.parents('.ui-dialog:first').next()[0].style.setProperty("z-index", zIndex, "important")
    }

    /* Returns the maximum zIndex of elements in the DOM.
     * This is needed because entellitrak uses ever-increasing zIndexes for its menus. */
    function maximumZIndex() {
        /* this is the zindex adds to the ui-front. it needs to be hardcoded here because it exists
         * in the DOM only after the dialog is created */
        var currentMaximumZIndex = 22222

        jQuery('*')
            .each(function(_index, ele) {
                var curZIndex = Number(jQuery(ele).css('z-index'))
                if (curZIndex > currentMaximumZIndex) {
                    currentMaximumZIndex = curZIndex
                }
            })

        return currentMaximumZIndex

    }

    function addLinkToSelect(fieldName, urlPrefix) {
        var linkHolder = jQuery("<div>")
            .css({
                display: "inline-block",
                marginLeft: "0.25em"
            })

        jQuery("[name=" + fieldName + "]")
            .parent()
            .append(linkHolder)

        jQuery("#" + fieldName + "").change(refreshLink)
        refreshLink()

        function refreshLink() {
            linkHolder.html("")

            var stateId = AeaFormUtilitiesJavascriptLibrary.getValue(fieldName)

            if (stateId != "") {
                linkHolder.append(
                    jQuery("<button>")
                    .addClass("formButton")
                    .prop({
                        type: "button"
                    })
                    .click(function() {
                        window.open(urlPrefix + encodeURIComponent(stateId), "_blank")
                    })
                    .text("View"))
            }
        }
    }

    function addLinksToMultiselect(fieldName, urlPrefix) {
        jQuery("#" + fieldName + "_multiValue > fieldset > label").each(function(_index, label) {
            var value = jQuery(label).find("input[name=" + fieldName + "]").val()

            jQuery(label).append(jQuery("<a>")
                .css({
                    textDecoration: "none",
                    marginLeft: ".25em"
                })
                .prop({
                    href: urlPrefix + encodeURIComponent(value),
                    target: "_blank"
                })
                .text("View"))
        })
    }

    function ensureMultiHeader(inputId) {
        var multiValueDiv = jQuery("#" + inputId + "_multiValue")

        var headerDiv = multiValueDiv.prev(".multi-header")

        if (headerDiv.length == 0) {
            return jQuery("<div>")
                .addClass("multi-header")
                .css({
                    backgroundColor: "#eeeeee",
                    border: "1px solid #b9b9b9",
                    borderRadius: "0.3em",
                    marginBottom: "0.2em",
                    padding: "0.1em"
                })
                .insertBefore(multiValueDiv)
        } else {
            return headerDiv
        }
    }

    function createMultiSelectAllNone(inputId) {
        var multiValueDiv = jQuery("#" + inputId + "_multiValue")

        ensureMultiHeader(inputId)
            .append(jQuery("<label>")
                .css({
                    marginRight: "1em"
                })
                .append(jQuery("<input>")
                    .prop({
                        type: "checkbox"
                    }))
                .change(function(event) {
                    var isChecked = jQuery(event.target).is(":checked")

                    jQuery(multiValueDiv)
                        .find("label[for^=" + inputId + "_]")
                        .filter(function(_index, label) {
                            return !jQuery(label).hasClass("aea-filtered")
                        }).find("[name=" + inputId + "]:checkbox")
                        .map(function(_index, checkbox) {
                            jQuery(checkbox).prop({
                                checked: isChecked
                            })
                        })
                })
                .append(jQuery("<span>")
                    .text("Select All")))
    }

    function createMultiFilter(inputId) {
        var multiValueDiv = jQuery("#" + inputId + "_multiValue")

        jQuery(multiValueDiv)
            .find(":checkbox[name=" + inputId + "]")
            .change(function(event) {
                if (jQuery(event.target).parents("label.aea-filtered").length > 0) {
                    jQuery(event.target).prop({
                        checked: !jQuery(event.target).is(":checked")
                    })
                }
            })

        ensureMultiHeader(inputId)
            .append(jQuery("<label>")
                .append(jQuery("<span>")
                    .text("Filter:"))
                .append(jQuery("<input>")
                    .prop({
                        type: "text"
                    }))
                .keyup(function(event) {
                    var filterText = jQuery(event.target).val()
                    multiValueDiv
                        .find("label")
                        .each(function(_index, ele) {
                            var jEle = jQuery(ele)
                            if (jEle.text().indexOf(filterText) >= 0) {
                                jEle
                                    .removeClass("aea-filtered")
                                    .css({
                                        opacity: 1,
                                        color: "#000"
                                    })
                            } else {
                                jEle
                                    .addClass("aea-filtered")
                                    .css({
                                        // We would just set the opacity, but IE8 dosen't have it
                                        opacity: 0.5,
                                        color: "#888"
                                    })
                            }
                        })
                }))
    }

    function getCoreCsrfToken() {
        return document.querySelector("input[name='com.tylertech.entellitrak.mvc.taglib.html.TOKEN']").value
    }

    return {
        reloadFunctions: reloadFunctions,
        addOnreloadEvent: addOnreloadEvent,
        addMultiloadEvent: addMultiloadEvent,
        autoResizeMultiSelects: autoResizeMultiSelects,
        isCreate: isCreate,
        loadStyleSheet: loadStyleSheet,
        lookupValueInArray: lookupValueInArray,
        getValue: getValue,
        filterArray: filterArray,
        nvl: nvl,
        nvl2: nvl2,
        addLinkToSelect: addLinkToSelect,
        addLinksToMultiselect: addLinksToMultiselect,
        createMultiSelectAllNone: createMultiSelectAllNone,
        createMultiFilter: createMultiFilter,
        generateRequiredIcon: generateRequiredIcon,
        appendTD: appendTD,
        createFileLink: createFileLink,
        maximumZIndex: maximumZIndex,
        dialogAlert: dialogAlert,
        getCoreCsrfToken: getCoreCsrfToken
    }
}(jQuery, AeaFormUtilitiesJavascriptLibrary)