from odoo import models


class Existing(models.Model):
    _name = 'existing'

for name in ["one"]:
    class Wildcard(models.Model):
        _name = 'wildcard.%s' % name

class WildcardDefault(models.Model):
    _name = 'mixed_wildcard.%s' % name

class NonWildcard(models.Model):
    _name = 'mixed_wildcard.explicit'