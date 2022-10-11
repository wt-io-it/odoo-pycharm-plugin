from odoo.models import Model
from odoo import api, SUPERUSER_ID


class MyModel(Model):

    def action_test_goto(self):
        env = api.Environment(self.env.cr, SUPERUSER_ID, {})
        env.ref('addon1.rec<caret>ord1')