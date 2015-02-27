define("ace/mode/hex_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

var HexHighlightRules = function() {

    this.$rules = {
        "start" : [
            {
                token : "comment",
                regex : /[0-9a-f]{8}/,
                next  : "hexValue"  
            }
        ],
        "hexValue" : [
            {
                token : "variable",
                regex : /[ \t][ 0-9A-F]+/,
                next  : "textValue"
            }
        ],
        "textValue" : [
            {
                token : "string",
                regex : /\|.*\|/,
                next  : "start"
            }
        ]           
    };

};

oop.inherits(HexHighlightRules, TextHighlightRules);

exports.HexHighlightRules = HexHighlightRules;
});

define("ace/mode/hex",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/hex_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;
var HexHighlightRules = require("./hex_highlight_rules").HexHighlightRules;

var Mode = function() {
    this.HighlightRules = HexHighlightRules;
};
oop.inherits(Mode, TextMode);

(function() {
    this.$id = "ace/mode/hex";
}).call(Mode.prototype);

exports.Mode = Mode;
});
