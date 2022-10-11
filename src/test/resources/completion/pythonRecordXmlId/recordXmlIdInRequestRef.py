from odoo.http import Controller, request


class MyController(Controller):

    def get_something(self):
        request.env.ref("addon1.auto<caret>")
