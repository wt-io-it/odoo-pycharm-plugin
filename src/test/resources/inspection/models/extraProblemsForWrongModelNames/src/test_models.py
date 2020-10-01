from odoo import models, fields

def _selection_values(self):
    return {r.id: r.name for r in self.env['not_existing_model']}

class MyModel(models.Model):
    _name='my_correct_model_name'

    my_self = fields.Many2one('my_correct_model_name', default=lambda self: self._default_value())
    my_other = fields.Many2one('idontknow', default=lambda self: self._default_value())
    my_selection = fields.Selection(_selection_values)

    def _default_value(self):
        return self.env['my_correct_model_name'].search([], limit=1)

    def _my_method_with_a_wrong_modules(self, model):
        try:
            if self.env['not_existing_model']:
                return True
        except KeyError:
            pass
        return True

