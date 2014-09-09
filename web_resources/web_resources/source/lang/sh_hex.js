if (! this.sh_languages) {
  this.sh_languages = {};
}
sh_languages['hex'] = [
  [
    [
      /([0-9a-f]{8})([ \t][ 0-9A-F]+)([ \t]\|.*\|)/g,
      ['sh_linenum', 'sh_symbol', 'sh_string'],
      -1
    ]
  ]
];
