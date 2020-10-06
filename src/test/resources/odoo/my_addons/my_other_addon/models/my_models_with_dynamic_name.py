from odoo import models
from odoo.tools import pycompat


model_data = {'model': 'custom_model'}

class CustomModel(models.Model):
    _name = model_data['model']

class CustomModel2(models.Model):
    _name = str(model_data['model'])

class CustomModel3(models.Model):
    _name = pycompat.to_native(model_data['model'])

try:
    class CustomModel4(models.Model):
        _name = pycompat.to_native(model_data['model'])
except:
    pass

class OuterCustomModel(models.Model):
    _name = 'outer_model'

    def _instanciate(self, model_data):
        class InnerCustomModel(models.Model):
            _name = pycompat.to_native(model_data['model'])

        return InnerCustomModel
