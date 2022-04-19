from odoo import models

class InheritedBase(models.Model):
    _name = 'inherited'

class InheritedInherited(models.Model):
    _inherit = 'inherited'

class InheritedInherited2(models.Model):
    _inherit = ("inherited")
