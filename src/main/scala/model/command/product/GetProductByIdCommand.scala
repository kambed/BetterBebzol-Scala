package model.command.product

import model.command.abstracts.BaseCommand

case class GetProductByIdCommand(productId: Long, userId: Long) extends BaseCommand
