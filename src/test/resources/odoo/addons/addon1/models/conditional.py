from odoo import models
from odoo.addons.addon1.models.existing import Existing


class ParentModelTrueForConditionalExpressionName(models.Model):
    _name = 'testing_conditional_expressions_true'

class ParentModelFalseForConditionalExpressionName(models.Model):
    _name = 'testing_conditional_expressions_false'

class AModelWithAConditionalExpressionName(models.Model):
    # https://github.com/wt-io-it/odoo-pycharm-plugin/issues/32
    # if one defines a model with a conditional expression, we should not throw an error
    _inherit = 'testing_conditional_expressions_true' if hasattr(Existing, "conditional") else 'testing_conditional_expressions_false'