from odoo import models, fields


class Existing(models.Model):
    _name = 'existing'
    name = fields.Char()

for name in ["one"]:
    class Wildcard(models.Model):
        _name = 'wildcard.%s' % name

class WildcardDefault(models.Model):
    _name = 'mixed_wildcard.%s' % name

class NonWildcard(models.Model):
    _name = 'mixed_wildcard.explicit'

class WellDescribed(models.Model):
    """A model containing a python string description"""
    _name = 'well_described'

class ModelWithoutAName(models.Model):
    pass
