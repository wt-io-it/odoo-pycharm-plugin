from odoo import models


variable = "1"
def read():
    return variable

def write(value):
    global variable
    variable = value

METHODS = {
    "read": read,
    "write": write
}

class ModelNameInInherit(models.Model):
    _name = 'model.name.in.inherit'

    def _get_something(self):
        METHODS["write"]("val<caret>")
