from odoo import models


class AnIncompleteModel(models.Model):
    # this is an incomplete model to simulate someone editing the file and adding an `_inherit =` before the `_inherits`
    # this is not valid for Odoo (you would also need a _name for this to "work"), it raises Errors in this case
    # TODO we should display a warning for this
    _inherit = _inherits = {'inherited': 'inherited_id'}


class AnIncorrectModel(models.Model):
    # if somebody misses the s in _inherits only the key of the dict is used in odoo
    # TODO we should display a warning for this
    _inherit = {'inherited': 'inherited_id'}


class AnotherIncorrectModel(models.Model):
    # this model is fixed by the _name in comparison with AnIncompleteModel
    # and it takes it's name from _name
    _name = 'strangely_inherited'
    _inherit = _inherits = {'inherited': 'inherited_id'}

