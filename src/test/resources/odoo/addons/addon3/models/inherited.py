from odoo import models

class InheritedBase(models.Model):
    _name = 'inherited'

class InheritedInherited(models.Model):
    _inherit = 'inherited'

class InheritedInheritedParentheses(models.Model):
    _inherit = ("inherited")

class InheritedInheritedTuple(models.Model):
    _inherit = "inherited",

class InheritedInheritedTupleParentheses(models.Model):
    _inherit = ("inherited",)

class InheritedInheritedSetExpression(models.Model):
    _inherit = {"inherited"}
