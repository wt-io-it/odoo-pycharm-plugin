from odoo.http import Controller, request


class MyController(Controller):

    def controller_test_goto(self):
        request.env.ref('addon1.rec<caret>ord1')