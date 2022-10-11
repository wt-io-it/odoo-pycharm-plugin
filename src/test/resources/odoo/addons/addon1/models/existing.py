from odoo import models, fields


class Existing(models.Model):
    _name = 'existing'
    name = fields.Char()

for name in ["one"]:
    class Wildcard(models.Model):
        _name = 'single_wildcard.%s' % name

class WildcardDefault(models.Model):
    _name = 'mixed_wildcard.default_%s' % name

class NonWildcardDefault(models.Model):
    _name = 'mixed_wildcard.default_explicit'

class WildcardFormatString(models.Model):
    _name = f'mixed_wildcard.format_{name}'

class NonWildcardFormatString(models.Model):
    _name = f'mixed_wildcard.format_explicit'


class WellDescribed(models.Model):
    """A model containing a python string description"""
    _name = 'well_described'

class ModelWithoutAName(models.Model):
    pass
