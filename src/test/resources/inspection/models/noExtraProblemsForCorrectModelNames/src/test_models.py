from odoo import models, fields

class MyModel(models.Model):
    _name='my_correct_model_name'

    my_self = fields.Many2one('my_correct_model_name', default=lambda self: self._default_value())

    def _default_value(self):
        return self.env['my_correct_model_name'].search([], limit=1)

    def _model_exists(self, model):
        try:
            if self.env[model]:
                return True
        except KeyError:
            pass
        return True

    def _model_has_template(self, model):
        try:
            if self.env[model + '.template']:
                return True
        except KeyError:
            pass
        return True

for name in ['one', 'two', 'three']:
    class MyGenericModelDefinition(models.Model):
        _name = 'package.%s' % name

class MyModelReferencesPackages(models.Model):
    _name = 'package.package'

    one = fields.Many2one('package.one')
    two = fields.Many2one('package.two')
    three = fields.Many2one('package.three')