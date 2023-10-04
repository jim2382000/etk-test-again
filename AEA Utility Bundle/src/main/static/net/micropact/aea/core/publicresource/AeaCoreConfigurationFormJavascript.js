/* public-resource */

"use strict";

(function(Tooltip) {
    jQuery(addTooltips)

    function addTooltips() {
        Tooltip.create()
            .done(function(tt) {
                tt.addSmartTips()
            })
    }
}(Tooltip))
